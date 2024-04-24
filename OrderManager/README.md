# Getting Started

### Overview

The OrderManager JAVA service is the orchestrator and coordinator service for the Ordering Process Transaction

The Transaction starts in this service and the other services participate in this transaction as composite transactions.

If any of the participant services fail then the entire global transaction rollbacks.

Atomikos is used for managing global and composite transactions.

![Alt text](./overview.png?raw=true "Overview")

### Prerequisites

1. MySQL - An active MySQL instance running (Any other RDBMS can be used which support XA).
2. Database - Create a Database in the MySQl instance
3. Ensure that the DB server/port/user/pwd are updated in the application.properties
        

### Reference Documentation

For further reference, please consider the following sections:

* [Transactional REST microservices with Atomikos](https://www.atomikos.com/Blog/TransactionalRESTMicroservicesWithAtomikos)
* [Distributed Transaction Propagation and Coordination](https://docs.google.com/presentation/d/e/2PACX-1vQZwBijx9t7xSfuRKMOnCXJPw_Vs1wf7lVekhbG2HJ0RPuNmB15BgnfXotdMT3hixQHoGKLog3M8Xsu/pub?start=true&loop=true&delayms=3000#slide=id.g2cc1439e7cf_0_15)


