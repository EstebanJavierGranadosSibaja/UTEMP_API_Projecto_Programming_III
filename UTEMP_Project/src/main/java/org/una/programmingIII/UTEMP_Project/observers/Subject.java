package org.una.programmingIII.UTEMP_Project.observers;

import java.util.ArrayList;
import java.util.List;

public class Subject<T extends Observer> {

    private final List<T> observers = new ArrayList<>();

    public synchronized void addObserver(T observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(T observer) {
        observers.remove(observer);
    }

    public synchronized void notifyObservers(String eventType, String message, String mail) {
        for (T observer : observers) {
            if (observer != null) {
                observer.update(eventType, message, mail);
            }
        }
    }
}
