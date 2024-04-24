package com.knowis.ordermanager.config;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

//@Component
public class TransactionHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public TransactionHttpMessageConverter() {
        super(MediaType.valueOf("application/vnd.atomikos+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // This converter is generic and can support any type of class
        return true;
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {


        String content = getStringFromInputStream(inputMessage.getBody());

        // Split content if it contains "|", otherwise return the whole content
        return content.contains("|") ? content.split(Pattern.quote("|")) : content;
    }

    @Override
    protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException {

        OutputStream outputStream = outputMessage.getBody();
        if (object instanceof Object[]) {
            // Join array elements with a "|" if it's an array
            Object[] array = (Object[]) object;
            for (int i = 0; i < array.length; i++) {
                outputStream.write(array[i].toString().getBytes(StandardCharsets.UTF_8));
                if (i < array.length - 1) {
                    outputStream.write("|".getBytes(StandardCharsets.UTF_8));
                }
            }
        } else {
            // Write the object as is, if it's not an array
            outputStream.write(object.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String getStringFromInputStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
