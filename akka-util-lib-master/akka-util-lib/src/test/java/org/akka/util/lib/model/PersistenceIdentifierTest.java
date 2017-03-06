package org.akka.util.lib.model;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistenceIdentifierTest {

    @Test
    public void highestSeqNrOrdered() throws Exception {

        List<PersistenceIdentifier> ids = Arrays.asList(new PersistenceIdentifier("1", "1", 1)
                , new PersistenceIdentifier("1", "2", 2)
                , new PersistenceIdentifier("1", "3", 3));

        Assertions.assertThat(PersistenceIdentifier.highestSeqNr(ids)).isEqualTo(3);

    }

    @Test
    public void highestSeqNrReverseOrdered() throws Exception {

        List<PersistenceIdentifier> ids = Arrays.asList(new PersistenceIdentifier("1", "1", 3)
                , new PersistenceIdentifier("1", "2", 2)
                , new PersistenceIdentifier("1", "3", 1));

        Assertions.assertThat(PersistenceIdentifier.highestSeqNr(ids)).isEqualTo(3);

    }

    @Test
    public void highestSeqNrMixed() throws Exception {

        List<PersistenceIdentifier> ids = Arrays.asList(new PersistenceIdentifier("1", "1", 1)
                , new PersistenceIdentifier("1", "2", 3)
                , new PersistenceIdentifier("1", "3", 2));

        Assertions.assertThat(PersistenceIdentifier.highestSeqNr(ids)).isEqualTo(3);

    }

    @Test
    public void highestSeqNrOneElement() throws Exception {

        List<PersistenceIdentifier> ids = Arrays.asList(new PersistenceIdentifier("1", "1", 1));

        Assertions.assertThat(PersistenceIdentifier.highestSeqNr(ids)).isEqualTo(1);

    }

    @Test
    public void highestSeqNrZeroElement() throws Exception {

        List<PersistenceIdentifier> ids = Arrays.asList();

        Assertions.assertThat(PersistenceIdentifier.highestSeqNr(ids)).isEqualTo(Long.MIN_VALUE);

    }
}
