package akka.initializer.model;

import akka.initializer.MessageExpiryEvent;
import org.junit.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MessageExpiryListenerTest {

    private MessageExpiryListener listener = new MessageExpiryListener() {

        @Override
        public long expirySequenceNr(Time timeWindow) {
            return 0;
        }

        @Override
        public void cleanupState(long toSequenceNr) {

        }
    };

    @Test
    public void highestSeqNrOrdered() throws Exception {

        // Given
        MessageExpiryEvent messageExpiryEvent1 = spy(new MessageExpiryEvent(1, "2", 100, null, "2016-11-07T17:30:27.001Z"));
        doReturn(Instant.parse("2016-11-07T17:30:27.055Z")).when(messageExpiryEvent1).currentTime();
        messageExpiryEvent1.setSequenceNr(1);
        MessageExpiryEvent messageExpiryEvent2 = spy(new MessageExpiryEvent(1, "2", 101, null, "2016-11-07T17:30:27.050Z"));
        doReturn(Instant.parse("2016-11-07T17:30:27.055Z")).when(messageExpiryEvent2).currentTime();
        messageExpiryEvent2.setSequenceNr(2);
        when(messageExpiryEvent2.getCreateTime()).thenReturn(Instant.parse("2016-11-07T17:30:27.052Z"));
        List<MessageExpiryEvent> messageExpiryEvents = Arrays.asList(messageExpiryEvent1, messageExpiryEvent2);

        // When
        Time time = new Time(1, TimeUnit.MILLISECONDS);
        long highestSequenceNr = listener.highestSequenceNr(messageExpiryEvents, time);
        // Then
        assertThat(highestSequenceNr).isEqualTo(2);

        // When
        time = new Time(25, TimeUnit.MILLISECONDS);
        highestSequenceNr = listener.highestSequenceNr(messageExpiryEvents, time);
        // Then
        assertThat(highestSequenceNr).isEqualTo(1);

        // When
        time = new Time(56, TimeUnit.MILLISECONDS);
        highestSequenceNr = listener.highestSequenceNr(messageExpiryEvents, time);
        // Then
        assertThat(highestSequenceNr).isEqualTo(Long.MIN_VALUE);

    }

    @Test
    public void cleanupList() throws Exception {

        // Given
        MessageExpiryEvent messageExpiryEvent1 = new MessageExpiryEvent(1, "2", 100, null, "2016-11-07T17:30:27.001Z");
        messageExpiryEvent1.setSequenceNr(1);
        MessageExpiryEvent messageExpiryEvent2 = new MessageExpiryEvent(1, "2", 101, null, "2016-11-07T17:30:27.050Z");
        messageExpiryEvent2.setSequenceNr(2);
        List<MessageExpiryEvent> messageExpiryEvents = Arrays.asList(messageExpiryEvent1, messageExpiryEvent2);

        // When, Then
        List<MessageExpiryEvent> result = listener.cleanupList(messageExpiryEvents, 1);
        assertThat(result.size()).isEqualTo(1);
        result = listener.cleanupList(messageExpiryEvents, 2);
        assertThat(result.size()).isEqualTo(0);
        assertThat(messageExpiryEvents.size()).isEqualTo(2);

    }

    @Test
    public void cleanupSet() throws Exception {

        // Given
        MessageExpiryEvent messageExpiryEvent1 = new MessageExpiryEvent(1, "2", 100, null, "2016-11-07T17:30:27.001Z");
        messageExpiryEvent1.setSequenceNr(1);
        MessageExpiryEvent messageExpiryEvent2 = new MessageExpiryEvent(1, "2", 101, null, "2016-11-07T17:30:27.050Z");
        messageExpiryEvent2.setSequenceNr(2);
        Set<MessageExpiryEvent> messageExpiryEvents = new HashSet<>(Arrays.asList(messageExpiryEvent1, messageExpiryEvent2));

        // When, Then
        Set<MessageExpiryEvent> result = listener.cleanupSet(messageExpiryEvents, 1);
        assertThat(result.size()).isEqualTo(1);
        result = listener.cleanupSet(messageExpiryEvents, 2);
        assertThat(result.size()).isEqualTo(0);
        assertThat(messageExpiryEvents.size()).isEqualTo(2);

    }


    @Test
    public void cleanupMap() throws Exception {

        // Given
        MessageExpiryEvent messageExpiryEvent1 = new MessageExpiryEvent(1, "2", 100, null, "2016-11-07T17:30:27.001Z");
        messageExpiryEvent1.setSequenceNr(1);
        MessageExpiryEvent messageExpiryEvent2 = new MessageExpiryEvent(1, "2", 101, null, "2016-11-07T17:30:27.050Z");
        messageExpiryEvent2.setSequenceNr(2);
        Map<Integer, MessageExpiryEvent> messageExpiryEvents = new HashMap<>();
        messageExpiryEvents.put(1, messageExpiryEvent1);
        messageExpiryEvents.put(2, messageExpiryEvent2);

        // When, Then
        Map<Integer, MessageExpiryEvent> result = listener.cleanupMap(messageExpiryEvents, 1);
        assertThat(result.size()).isEqualTo(1);
        result = listener.cleanupMap(messageExpiryEvents, 2);
        assertThat(result.size()).isEqualTo(0);
        assertThat(messageExpiryEvents.size()).isEqualTo(2);

    }
}
