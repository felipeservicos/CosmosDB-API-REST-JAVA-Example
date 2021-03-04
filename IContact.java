package com.example.contacts.repository;

import com.example.contacts.model.Contact;
import com.example.contacts.model.ListOfContactWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IContact {
    ResponseEntity<Contact> findContactById(String idContact) ;
    ResponseEntity<ListOfContactWrapper> getAllContacts();
    ResponseEntity<Contact> saveContact(Contact contact);
    ResponseEntity<Contact> updateContact(Contact contact);
    ResponseEntity<String> deleteContact(String idContact);
    ResponseEntity<String> createCollection(String name);
    ResponseEntity<String> getCollection(String name) ;
    ResponseEntity<String> getAllCollections();
    ResponseEntity<String> deleteCollection(String name);
    ResponseEntity<String> createDB(String dbName) ;
    ResponseEntity<String> deleteDB(String dbName) ;
    String listDatabases() ;



}
