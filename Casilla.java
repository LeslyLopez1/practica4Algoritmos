package com.example.practica4listas;

public class Casilla implements Comparable<Casilla> {

    private Nodo nodo;
    private boolean eliminada;
    public Casilla(int numero) {
        this.nodo     = new Nodo(numero);
        this.eliminada = false;
    }

    @Override
    public int compareTo(Casilla otra) {
        if (otra == null || otra.eliminada) return -1;
        return nodo.isMatchValue(otra.nodo) ? 0 : -1;
    }

    public boolean concuerdaCon(Casilla otra) {
        return this.compareTo(otra) == 0;
    }

    public boolean esVecina(Casilla otra) {
        if (otra == null || otra.eliminada || this.eliminada) return false;
        return nodo.isNeighbor(otra.nodo);
    }

    public void eliminar() {
        nodo.delete();
        eliminada = true;
    }

    public void restaurar() {
        eliminada = false;
    }

    public Nodo getNodo() {
        return nodo;
    }

    //getters
    public int     getNumero()   { return nodo.getNumber(); }
    public boolean isEliminada() { return eliminada; }

    @Override
    public String toString() { return nodo.toString(); }
}