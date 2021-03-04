package com.example.contacts.repository;

import com.example.utils.Converters;
import com.example.contacts.model.Contact;
import com.example.contacts.model.ListOfContactWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;




@Configuration
@Component

public class RepositoryCosmosDB implements IContact {


	//Configure your application.properties with Azure information/credentials.
    @Value("${azure.cosmosdb.uri}")
    private String urlBase;
    @Value("${azure.cosmosdb.key}")
    private String key;
	@Value("${azure.cosmosdb.database}")	//Setup here any db name
    private String defaultDatabaseName;
    @Value("${azure.cosmosdb.defaultcollection}") //Setup here any collection name. In my case, simple Contact collection. 
    private String defaultCollection;



    HttpHeaders headers;

    public RepositoryCosmosDB() {
        headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("x-ms-version", "2016-07-11");


    }

    @Override
    public ResponseEntity<Contact> findContactById(String idContact) {


        //Setup parameter auth and URL mount
        final String method = "get";
        final String resourceType = "docs";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + defaultCollection + "/" + resourceType + "/" + idContact;
        final String url = urlBase + resourceLink;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);


        try {
            headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Contact> result = restTemplate.exchange(url, HttpMethod.GET, entity, Contact.class);

        return result;
    }

    @Override
    public ResponseEntity<ListOfContactWrapper> getAllContacts() {


        //Setup parameter auth and URL mount
        final String method = "get";
        final String resourceType = "docs";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + defaultCollection;
        final String url = urlBase + resourceLink + "/" + resourceType;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<ListOfContactWrapper> result = restTemplate.exchange(url, HttpMethod.GET, entity, ListOfContactWrapper.class);


        return result;
    }


    @Override
    public ResponseEntity<Contact> saveContact(Contact contact) {

        //Prevent nullpointer
        Optional<Contact> ContactOptional = Optional.ofNullable(contact);
        if (!ContactOptional.isPresent()) return null;


        final String resourceType = "docs";
        final String method = "post";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + defaultCollection;
        final String url = urlBase + resourceLink + "/" + resourceType;


        //Setup parameter auth and URL mount
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);
        final String bodyPayload = Converters.toJson(contact);


        try {
            headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(bodyPayload, headers);
        ResponseEntity<Contact> result = restTemplate.exchange(url, HttpMethod.POST, entity, Contact.class);

        return result;
    }

    @Override
    public ResponseEntity<Contact> updateContact(Contact contact) {

        //Prevent nullpointer
        Optional<Contact> ContactOptional = Optional.ofNullable(contact);
        if (!ContactOptional.isPresent()) return null;

        final String resourceType = "docs";
        final String method = "put";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + defaultCollection + "/" + resourceType + "/" + ContactOptional.get().getId();
        final String url = urlBase + resourceLink;


        //Setup parameter auth and URL mount
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);
        final String bodyPayload = Converters.toJson(contact);


        try {
            headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(bodyPayload, headers);
        ResponseEntity<Contact> result = restTemplate.exchange(url,HttpMethod.PUT, entity, Contact.class);

        return result;
    }

    @Override
    public ResponseEntity<String> deleteContact(String idContact)  {
        //Setup parameter auth and URL mount
        final String method = "delete";
        final String resourceType = "docs";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + defaultCollection + "/" + resourceType + "/" + idContact;
        final String url = urlBase + resourceLink;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        return result;
    }


    @Override
    public ResponseEntity<String> createCollection(String name)  {


        //Setup parameter auth and URL mount
        final String method = "post";
        final String resourceType = "colls";
        final String resourceLink = "dbs/" + defaultDatabaseName;
        final String url = urlBase + resourceLink + "/colls";
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);
        final String bodyPayload = String.format("{\"%s\" : \"%s\"}", "id", name);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(bodyPayload, headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return result;

    }

    public ResponseEntity<String> createCollectionInDatabase(String collectionName, String dbName)  {


        //Setup parameter auth and URL mount
        final String method = "post";
        final String resourceType = "colls";
        final String resourceLink = "dbs/" + dbName;
        final String url = urlBase + resourceLink + "/colls";
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);
        final String bodyPayload = String.format("{\"%s\" : \"%s\"}", "id", collectionName);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(bodyPayload, headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return result;

    }

    @Override
    public ResponseEntity<String> getCollection(String name)  {


        //Setup parameter auth and URL mount
        final String method = "get";
        final String resourceType = "colls";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + name;
        final String url = urlBase + "dbs/" + defaultDatabaseName + "/colls/" + name;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);

        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);


        return result;

    }

    @Override
    public ResponseEntity<String> getAllCollections()  {
        //Setup parameter auth and URL mount
        final String method = "get";
        final String resourceType = "colls";
        final String resourceLink = "dbs/" + defaultDatabaseName;
        final String url = urlBase + "dbs/" + defaultDatabaseName + "/colls";
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return result;
    }

    @Override
    public ResponseEntity<String> deleteCollection(String name)  {


        //Setup parameter auth and URL mount
        final String method = "delete";
        final String resourceType = "colls";
        final String resourceLink = "dbs/" + defaultDatabaseName + "/colls/" + name;
        final String url = urlBase + "dbs/" + defaultDatabaseName + "/colls/" + name;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);


        return result;

    }

    @Override
    public ResponseEntity<String> createDB(String dbName)  {


        //Setup parameter auth and URL mount
        final String method = "post";
        final String resourceType = "dbs";
        final String resourceLink = "";
        final String url = urlBase + resourceType;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);
        final String bodyPayload = String.format("{\"%s\" : \"%s\"}", "id", dbName);


        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(bodyPayload, headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return result;

    }

    @Override
    public ResponseEntity<String> deleteDB(String dbName)  {

        //Setup parameter auth and URL mount
        final String method = "delete";
        final String resourceType = "dbs";
        final String resourceLink = "dbs/" + dbName;
        final String url = urlBase + resourceLink;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);

        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);


        return result;

    }

    @Override
    public String listDatabases()  {

        //Setup parameter auth and URL mount
        final String method = "get";
        final String resourceType = "dbs";
        final String resourceLink = "";
        final String url = urlBase + resourceType;
        final String stringToSign = mountStringToSign(method, resourceType, resourceLink);

        headers.add("Authorization", getAuthenticationString(stringToSign, key.getBytes()));
        headers.add("x-ms-date", getDateRFC());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return result.getBody();
    }

    public String mountStringToSign(String method, String resourceType, String resourceLink) {

        String stringToSign = method.toLowerCase() + "\n"
                + resourceType.toLowerCase() + "\n"
                + (resourceLink.toLowerCase().isEmpty() ? "" : resourceLink) + "\n"
                + getDateRFC().toLowerCase() + "\n"
                + "" + "\n";

        return stringToSign;
    }

    public String getDateRFC() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        return dateFormat.format(calendar.getTime());
    }


    public String getAuthenticationString(String stringToSign, byte[] key)  {
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            mac.init(new SecretKeySpec(Base64.decode(key), "HmacSHA256"));
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        String authKey = null;
        try {
            authKey = new String(Base64.encode(mac.doFinal(stringToSign.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String auth = "type=master&ver=1.0&sig=" + authKey;
        auth = URLEncoder.encode(auth);
        return auth;
    }


    //To get data from application.properties.
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
