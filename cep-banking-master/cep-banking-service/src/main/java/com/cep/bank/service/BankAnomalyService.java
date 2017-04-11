package com.cep.bank.service;


import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import com.cep.bank.service.domain.CustomerTransactionEvent;
import com.cep.bank.service.domain.GenericEvent;
import akka.sdk.AkkaManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankAnomalyService {

    @Autowired
    private AkkaManager akkaManager;

    public void handleCustomerTransactionEvent(CustomerTransactionEvent customerTransactionEvent, ActorRef responseActor) {

        bankAndCreditCardAnomalyDetectorClusterShard().tell(customerTransactionEvent, responseActor);

    }


    public void handleGenericEvent(GenericEvent genericEvent, ActorRef responseActor) {

        bankAndCreditCardAnomalyDetectorClusterShard().tell(genericEvent, responseActor);
    }


    private ActorRef bankAndCreditCardAnomalyDetectorClusterShard() {

        return ClusterSharding.get(akkaManager.getActorSystem()).shardRegion("BankAndCreditCardShardRegion");


    }
}
