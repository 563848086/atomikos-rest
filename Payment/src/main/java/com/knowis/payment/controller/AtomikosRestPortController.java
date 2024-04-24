package com.knowis.payment.controller;

import com.atomikos.icatch.*;
import com.atomikos.icatch.config.Configuration;
import com.atomikos.icatch.imp.CoordinatorImp;
import com.atomikos.logging.Logger;
import com.atomikos.logging.LoggerFactory;
import com.atomikos.recovery.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;

@RestController
@RequestMapping(value ="/payment/atomikos")
public class AtomikosRestPortController {

    private static final Logger LOGGER = LoggerFactory.createLogger(AtomikosRestPortController.class);

    public static String atomikosRestPortUrl;
    private static RecoveryLog recoveryLog;

    public AtomikosRestPortController() {
    }

    @Value("${com.atomikos.icatch.rest_port_url}")
    public void setAtomikosRestPortUrl(String url) {
        AtomikosRestPortController.atomikosRestPortUrl = url;
    }

    public static void setUrl(String url) {
        if (atomikosRestPortUrl == null && url != null) {
            atomikosRestPortUrl = url;
            if (!atomikosRestPortUrl.endsWith("/")) {
                atomikosRestPortUrl = atomikosRestPortUrl + "/";
            }
        }
        LOGGER.logInfo("------- Atomikos URL after set-------------");
        LOGGER.logInfo(atomikosRestPortUrl);
    }

    public static String getUrl() {
        LOGGER.logInfo("------- Getting Atomikos URL-------------");
        LOGGER.logInfo(atomikosRestPortUrl);
        return atomikosRestPortUrl;
    }

    public static String buildParticipantUrl(CompositeTransaction ct) throws SysException {
        assertRestPortUrlSet();
        return getUrl() + ct.getCompositeCoordinator().getRootId() + "/" + ct.getCompositeCoordinator().getCoordinatorId();
    }

    private static void assertRestPortUrlSet() {
        if (getUrl() == null) {
            throw new SysException("Please set property com.atomikos.icatch.rest_port_url - see https://www.atomikos.com/Documentation/ConfiguringRemoting for details");
        }
    }


    public static void init(String url) {
        recoveryLog = Configuration.getRecoveryLog();
        setUrl(url);
    }

    private static String buildParticipantUrl(String root, String coordinatorId) {
        LOGGER.logInfo("------- buildParticipantUrlL-------------");
        LOGGER.logInfo(atomikosRestPortUrl +"/"+ root + "/" + coordinatorId);
        return atomikosRestPortUrl +"/"+ root + "/" + coordinatorId;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Hello from Atomikos!";
    }

    @GetMapping(value = "/{coordinatorId}")
    public String getOutcome(@PathVariable("coordinatorId") String coordinatorId) {
        TxState ret = TxState.TERMINATED;
        PendingTransactionRecord record = null;

        try {
            record = recoveryLog.get(coordinatorId);
        } catch (LogReadException var5) {
            LOGGER.logWarning("Unexpected log exception", var5);
            this.throw409(var5);
        }

        if (record != null) {
            ret = record.state;
        }

        return ret.toString();
    }

    @PostMapping(path="/{rootId}/{coordinatorId}",consumes = "application/vnd.atomikos+json")
    public ResponseEntity<String> prepare(@PathVariable("rootId") String rootId, @PathVariable("coordinatorId") String coordinatorId, @RequestBody Map<String, Integer> cascadeList) {

        LOGGER.logInfo("prepare ( ... ) received for root " + rootId);
        TransactionService service = Configuration.getTransactionService();
        String extentUri = buildParticipantUrl(rootId, coordinatorId);

        Integer count = (Integer) cascadeList.get(extentUri);

        Participant part = service.getParticipant(rootId);
        if (part == null) {
            //return Response.status(Response.Status.NOT_FOUND).entity(rootId).build();
            return new ResponseEntity<>(rootId,HttpStatus.NOT_FOUND);
        } else {
            part.setGlobalSiblingCount(count);
            part.setCascadeList(cascadeList);
            int result = -1;

            try {
                result = part.prepare();
            } catch (RollbackException var10) {
                LOGGER.logWarning("Error in prepare for root " + rootId, var10);
                this.throw404();
            } catch (Exception var11) {
                LOGGER.logWarning("Error in prepare for root " + rootId, var11);
                this.throw409(var11);
            }

            return new ResponseEntity<>(String.valueOf(result),HttpStatus.CREATED);
        }
    }

    private void throw404() {
        Response response = Response.status(Response.Status.NOT_FOUND).entity("Transaction has timed out and was rolledback").type("text/plain").build();
        throw new WebApplicationException(response);
    }

    private void throw409(Exception e) {
        Response response = Response.status(Response.Status.CONFLICT).entity(e.getMessage()).type("text/plain").build();
        throw new WebApplicationException(response);
    }

    @PutMapping(value="/{rootId}/{coordinatorId}/{onePhase}")
    public Response commit(@PathVariable("rootId") String rootId, @PathVariable("coordinatorId") String coordinatorId, @PathVariable("onePhase") boolean onePhase) {
        LOGGER.logInfo("commit() received for root " + rootId + " onePhase = " + onePhase);

        TransactionService service = Configuration.getTransactionService();
        CoordinatorImp part = (CoordinatorImp)service.getParticipant(rootId);
        if (part != null) {
            if (!part.getState().isFinalState()) {
                if (!part.getState().transitionAllowedTo(TxState.COMMITTING)) {
                    if (!onePhase) {
                        LOGGER.logWarning("Commit no longer allowed for root " + rootId + " - probably due to heuristic rollback?");
                        return Response.status(Response.Status.CONFLICT).entity(rootId).build();
                    }
                } else {
                    try {
                        part.commit(onePhase);
                    } catch (RollbackException var8) {
                        LOGGER.logWarning("Error in commit for root " + rootId, var8);
                        this.throw404();
                    } catch (Exception var9) {
                        LOGGER.logWarning("Error in commit for root " + rootId, var9);
                        this.throw409(var9);
                    }
                }
            }
        } else {
            if (onePhase) {
                Response response = Response.status(Response.Status.CONFLICT).entity(rootId).type("text/plain").build();
                throw new WebApplicationException(response);
            }

            try {
                this.delegateToRecovery(coordinatorId, true);
            } catch (LogException var7) {
                LOGGER.logWarning("Error in commit for root " + rootId, var7);
                this.throw409(var7);
            }
        }
        LOGGER.logInfo("Commit Successful ");
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private void delegateToRecovery(String coordinatorId, boolean commit) throws LogException {
        if (recoveryLog == null) {
            recoveryLog = Configuration.getRecoveryLog();
        }

        if (commit) {
            recoveryLog.recordAsCommitting(coordinatorId);
        } else {
            recoveryLog.forget(coordinatorId);
        }

    }

    @DeleteMapping("/{rootId}/{coordinatorId}")
    public Response rollback(@PathVariable("rootId") String rootId, @PathVariable("coordinatorId") String coordinatorId) {

        LOGGER.logInfo("rollback() received for root " + rootId);

        TransactionService service = Configuration.getTransactionService();
        Participant part = service.getParticipant(rootId);
        if (part != null) {
            try {
                part.rollback();
            } catch (Exception var7) {
                LOGGER.logWarning("Error in rollback for root " + rootId, var7);
                this.throw409(var7);
            }
        } else {
            try {
                this.delegateToRecovery(coordinatorId, false);
            } catch (LogException var6) {
                LOGGER.logWarning("Error in rollback for root " + rootId, var6);
                this.throw409(var6);
            }
        }

        LOGGER.logInfo("Rollback Successful ");
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}

