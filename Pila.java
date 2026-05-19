package com.example.practica4listas;

public class Pila<T> {
    private T[] pila;
    private int tope = -1;

    public Pila() {
        pila = (T[]) new Object[100];
    }

    public Pila(int tamaño) {
        pila = (T[]) new Object[tamaño];
    }

    public void push(T elemento) {
        if (!pilaLlena()) {
            tope++;
            pila[tope] = elemento;
        }
    }

    public T pop() {
        if (pilaVacia()) return null;
        T dato = pila[tope];
        pila[tope] = null;
        tope--;
        return dato;
    }

    public T verTope() {
        return pilaVacia() ? null : pila[tope];
    }

    public boolean pilaVacia() { return tope == -1; }
    public boolean pilaLlena() { return tope == pila.length - 1; }
}