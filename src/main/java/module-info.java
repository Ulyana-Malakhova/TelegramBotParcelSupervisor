import org.example.api.tracking_api.TrackingApiClient;
import org.example.api.tracking_api.TrackingApiClientBasic;

module core {
    exports org.example;
    exports org.example.Service;
    exports org.example.Command;
    exports org.example.Repository;
    exports org.example.Dto;
    exports org.example.Entity;
    exports org.example.api;
    exports org.example.api.tracking_api;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires org.json;
    requires telegrambots;
    requires telegrambots.meta;
    requires org.apache.commons.lang3;
    requires jakarta.mail;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpclient;
    requires static lombok;
    requires jakarta.persistence;
    requires spring.data.jpa;
    requires spring.data.commons;
    requires jakarta.transaction;
    requires modelmapper;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires passay;
    requires spring.context.support;
    requires spring.security.crypto;
    requires spring.tx;
    requires org.hibernate.orm.core;
    requires tess4j;
    opens org.example.Entity;
    opens org.example to spring.core, spring.beans, spring.context, spring.boot;
    opens org.example.Command to spring.core, spring.beans, spring.context;

    uses TrackingApiClient;
    provides TrackingApiClient with TrackingApiClientBasic;
}