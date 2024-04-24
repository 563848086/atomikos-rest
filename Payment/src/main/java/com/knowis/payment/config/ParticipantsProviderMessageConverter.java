package com.knowis.payment.config;


import com.atomikos.logging.Logger;
import com.atomikos.logging.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class ParticipantsProviderMessageConverter extends AbstractHttpMessageConverter<Map<String, Integer>> {

    private static final Logger LOGGER = LoggerFactory.createLogger(ParticipantsProviderMessageConverter.class);

    public ParticipantsProviderMessageConverter() {
        super(MediaType.valueOf("application/vnd.atomikos+json"));

    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    @Override
    protected Map<String, Integer> readInternal(Class<? extends Map<String, Integer>> clazz, HttpInputMessage inputMessage)
            throws IOException {
        String content = getStringFromInputStream(inputMessage.getBody());
        LOGGER.logTrace("Incoming REST request payload:\n" + content);

        HashMap<String, Integer> result = new HashMap<>();
        String keyValues = content.replaceAll("\\{", "").replaceAll("\\}", "");
        for (String entry : keyValues.split(",")) {
            entry = entry.replaceAll("\"", "");
            String[] pair = entry.split("=");
            result.put(pair[0], Integer.valueOf(pair[1]));
        }

        return result;
    }

    @Override
    protected void writeInternal(Map<String, Integer> map, HttpOutputMessage outputMessage)
            throws IOException {
        LOGGER.logTrace("writeInternal method called.");
        StringBuffer buffer = new StringBuffer("{");
        String comma = "";
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            buffer.append(comma).append('{').append('"').append(entry.getKey()).append('"').append('=').append(entry.getValue()).append('}');
            comma = ",";
        }
        buffer.append("}");
        OutputStream outputStream = outputMessage.getBody();
        outputStream.write(buffer.toString().getBytes());
    }

    private String getStringFromInputStream(InputStream is) throws IOException {
        LOGGER.logTrace("getStringFromInputStream method called.");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }
}
