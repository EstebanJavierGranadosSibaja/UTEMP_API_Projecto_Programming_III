package org.una.programmingIII.UTEMP_Project.observers;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    private List<Observer> observers = new ArrayList<>();

    // Método para agregar un observador
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Método para eliminar un observador
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Método para notificar a todos los observadores
    public void notifyObservers(String eventType, String message, String mail) {
        for (Observer observer : observers) {
            observer.update(eventType, message, mail);
        }
    }
}
