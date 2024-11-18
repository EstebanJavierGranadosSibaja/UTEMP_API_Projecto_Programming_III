package org.una.programmingIII.UTEMP_Project.observers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;


class SubjectTest {

    private Subject<Observer> subject;
    private Observer observer1;
    private Observer observer2;

    @BeforeEach
    void setUp() {
        subject = new Subject<>();
        observer1 = mock(Observer.class);
        observer2 = mock(Observer.class);
    }

    @Test
    void testAddObserver() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);

        verify(observer1, never()).update(anyString(), anyString(), anyString());
        verify(observer2, never()).update(anyString(), anyString(), anyString());
    }

    @Test
    void testNotifyObservers() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);

        subject.notifyObservers("eventType", "Message", "mail@example.com");

        verify(observer1, times(1)).update("eventType", "Message", "mail@example.com");
        verify(observer2, times(1)).update("eventType", "Message", "mail@example.com");
    }

    @Test
    void testRemoveObserver() {
        subject.addObserver(observer1);
        subject.addObserver(observer2);

        subject.removeObserver(observer1);
        subject.notifyObservers("eventType", "Message", "mail@example.com");

        verify(observer1, never()).update(anyString(), anyString(), anyString());
        verify(observer2, times(1)).update("eventType", "Message", "mail@example.com");
    }

    @Test
    void testNotifyNullObserver() {
        subject.addObserver(null);
        subject.addObserver(observer1);

        subject.notifyObservers("eventType", "Message", "mail@example.com");

        verify(observer1, times(1)).update("eventType", "Message", "mail@example.com");
    }
}
