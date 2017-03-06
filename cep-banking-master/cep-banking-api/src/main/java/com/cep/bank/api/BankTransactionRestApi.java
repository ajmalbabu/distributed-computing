package com.cep.bank.api;

import akka.actor.ActorRef;
import com.cep.bank.service.BankAnomalyService;
import com.cep.bank.service.domain.CustomerTransactionEvent;
import com.cep.bank.service.domain.GenericEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Accepts customer transaction of type credit-card & bank transactions.
 */
@RestController
@RequestMapping("/v1")
public class BankTransactionRestApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransactionRestApi.class);

    @Autowired
    private BankAnomalyService bankAnomalyService;

    @RequestMapping(value = "customerTransactionEvent", method = RequestMethod.POST)
    public ResponseEntity<String> customerTransactionEvent(@RequestBody CustomerTransactionEvent customerTransactionEvent) throws Exception {

        LOGGER.info("API received {}", customerTransactionEvent);

        customerTransactionEvent.setEntityId(customerTransactionEvent.getCustomerName());

        bankAnomalyService.handleCustomerTransactionEvent(customerTransactionEvent, ActorRef.noSender());

        return new ResponseEntity<String>("Received: " + customerTransactionEvent.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "genericEvent", method = RequestMethod.POST)
    public ResponseEntity<String> genericEvent(@RequestBody GenericEvent genericEvent,
                                               @RequestParam("customer") List<String> customers) throws Exception {

        LOGGER.info("API received: {} for customers: {}", genericEvent, customers);

        // Use AKKA Ddata http://doc.akka.io/docs/akka/2.4.16/scala/distributed-data.html to send events to all nodes
        // instead of sending to each individual customer actors.

        customers.stream().forEach(customer -> {
            genericEvent.setEntityId(customer);
            LOGGER.info(genericEvent.getEntityId());
            bankAnomalyService.handleGenericEvent(genericEvent, ActorRef.noSender());
        });

        return new ResponseEntity<String>("Received: " + genericEvent.toString(), HttpStatus.OK);
    }


}
