package com.example.practica4listas;

import java.util.Random;

public class Juego {

    private int columnas;
    private ListaSimple tablero;
    private Pila<Casilla[]> pilaDeshacer;
    private int concordanciasEncontradas;
    private Casilla seleccionada;

    public Juego(int filas, int columnas) {
        this.columnas = columnas;
        tablero = new ListaSimple();
        pilaDeshacer = new Pila<>();
        concordanciasEncontradas = 0;
        seleccionada = null;
        iniciar(filas, columnas);
    }

    private void iniciar(int filas, int columnas) {
        int total = filas * columnas;
        int[] numeros = new int[total];

        for (int i = 0; i < total; i++) numeros[i] = (i % 9) + 1;

        // Mezclar aleatoriamente (Fisher-Yates)
        Random rnd = new Random();
        for (int i = total - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = numeros[i];
            numeros[i] = numeros[j];
            numeros[j] = tmp;
        }
        for (int i = 0; i < total; i++) tablero.insertaFinal(new Casilla(numeros[i]));
        enlazarTodo();
    }
    //enlace entre Neighobrs
    private void enlazarTodo() {
        int n     = tablero.size();
        int filas = (n + columnas - 1) / columnas;

        //limpia enlaces
        for (int i = 0; i < n; i++) {
            Nodo nd = getCasilla(i).getNodo();
            nd.setLeft(null);
            nd.setRight(null);
            nd.setUp(null);
            nd.setDown(null);
            nd.setUpLeft(null);
            nd.setUpRight(null);
            nd.setDownLeft(null);
            nd.setDownRight(null);
        }

        for (int i = 0; i < n; i++) {
            Casilla actual = getCasilla(i);
            if (!actual.isEliminada()) {
                Nodo nd  = actual.getNodo();
                int fila = i / columnas;
                int col  = i % columnas;

                //left
                //empieza en la columna hacia la izquierda, busca el vecino activo
                //empieza desde la columna anterior -1 y avanza en direccion izq. c--
                //mientras c2 >= 0 seguira iterando y cuando llegue al -1 la condicion
                //para y dejara de iterar porque ya no hay mas cols a la izq.
                //luego dentro del for la casilla lama al indice dando como parametro la
                //fila y la columna para buscar en ella c2, si c no esta eliminada entonces
                //el nodo se le asigna como izquierda, y asi en todos los lados
                for (int c2 = col - 1; c2 >= 0; c2--) {
                    Casilla c = getCasilla(fila * columnas + c2);
                    if (!c.isEliminada()) {
                        nd.setLeft(c.getNodo());
                        break;
                    }
                }
                //right
                for (int c2 = col + 1; c2 < columnas; c2++) {
                    int idx = fila * columnas + c2;
                    if (idx < n) {
                        Casilla c = getCasilla(idx);
                        if (!c.isEliminada()) {
                            nd.setRight(c.getNodo());
                            break;
                        }
                    }
                }
                //up
                for (int f2 = fila - 1; f2 >= 0; f2--) {
                    Casilla c = getCasilla(f2 * columnas + col);
                    if (!c.isEliminada()) {
                        nd.setUp(c.getNodo());
                        break;
                    }
                }
                //down
                for (int f2 = fila + 1; f2 < filas; f2++) {
                    int idx = f2 * columnas + col;
                    if (idx < n) {
                        Casilla c = getCasilla(idx);
                        if (!c.isEliminada()) {
                            nd.setDown(c.getNodo());
                            break;
                        }
                    }
                }
                //upLeft
                for (int f2 = fila-1, c2 = col-1; f2 >= 0 && c2 >= 0; f2--, c2--) {
                    Casilla c = getCasilla(f2 * columnas + c2);
                    if (!c.isEliminada()) {
                        nd.setUpLeft(c.getNodo());
                        break;
                    }
                }
                //upRight
                for (int f2 = fila-1, c2 = col+1; f2 >= 0 && c2 < columnas; f2--, c2++) {
                    int idx = f2 * columnas + c2;
                    if (idx < n) {
                        Casilla c = getCasilla(idx);
                        if (!c.isEliminada()) {
                            nd.setUpRight(c.getNodo());
                            break;
                        }
                    }
                }
                //downLeft
                for (int f2 = fila+1, c2 = col-1; f2 < filas && c2 >= 0; f2++, c2--) {
                    int idx = f2 * columnas + c2;
                    if (idx < n) {
                        Casilla c = getCasilla(idx);
                        if (!c.isEliminada()) {
                            nd.setDownLeft(c.getNodo());
                            break;
                        }
                    }
                }
                //downRight
                for (int f2 = fila+1, c2 = col+1; f2 < filas && c2 < columnas; f2++, c2++) {
                    int idx = f2 * columnas + c2;
                    if (idx < n) {
                        Casilla c = getCasilla(idx);
                        if (!c.isEliminada()) {
                            nd.setDownRight(c.getNodo());
                            break;
                        }
                    }
                }
            }
        }
    }

    //selección
    public Resultado seleccionar(Casilla c) {
        if (c == null || c.isEliminada()){
            return Resultado.INVALIDA;}

        if (seleccionada == null) {
            seleccionada = c;
            return Resultado.PRIMERA_SELECCION;
        }
        if (seleccionada == c) {
            seleccionada = null;
            return Resultado.DESELECCIONADA;
        }
        if (puedeConcordar(seleccionada, c)) {
            pilaDeshacer.push(new Casilla[]{ seleccionada, c });
            seleccionada.eliminar();
            c.eliminar();
            concordanciasEncontradas++;
            seleccionada = null;
            enlazarTodo();
            return Resultado.CONCORDANCIA;
        }
        seleccionada = c;
        return Resultado.SIN_CONCORDANCIA;
    }

    public boolean puedeConcordar(Casilla a, Casilla b) {
        if (a == null || b == null) return false;
        if (a.compareTo(b) != 0)    return false;
        return a.esVecina(b) || sonVecinasConsecutivas(a, b);
    }

    private boolean sonVecinasConsecutivas(Casilla a, Casilla b) {
        int ia = indiceDe(a), ib = indiceDe(b);
        if (ia < 0 || ib < 0) return false;

        int desde, hasta;
        if (ia < ib) {
            desde = ia;
            hasta = ib;
        } else {
            desde = ib;
            hasta = ia;
        }

        for (int i = desde + 1; i < hasta; i++) {
            if (!getCasilla(i).isEliminada()) return false;
        }
        return true;
    }
    //pista
    public Casilla[] getPista() {
        int n = tablero.size();
        for (int i = 0; i < n; i++) {
            Casilla a = getCasilla(i);
            if (!a.isEliminada()) {
                for (int j = i + 1; j < n; j++) {
                    Casilla b = getCasilla(j);
                    if (!b.isEliminada()) {
                        if (puedeConcordar(a, b)) return new Casilla[]{ a, b };
                    }
                }
            }
        }
        return null;
    }

    //Undo
    public Casilla[] deshacer() {
        if (pilaDeshacer.pilaVacia()) return null;
        Casilla[] par = pilaDeshacer.pop();
        par[0].restaurar();
        par[1].restaurar();
        concordanciasEncontradas--;
        enlazarTodo();
        return par;
    }

    //estado  juego
    public boolean sinMovimientos() { return getPista() == null && !todasEliminadas(); }

    public boolean todasEliminadas() {
        int n = tablero.size();
        for (int i = 0; i < n; i++) if (!getCasilla(i).isEliminada()) return false;
        return true;
    }

    //getters
    public int getConcordanciasEncontradas() { return concordanciasEncontradas; }

    public int getConcordanciasPendientes() {
        int activas = 0;
        int n = tablero.size();
        for (int i = 0; i < n; i++) if (!getCasilla(i).isEliminada()) activas++;
        return activas / 2;
    }

    public int getColumnas(){ return columnas; }
    public Casilla getSeleccionada(){ return seleccionada; }
    public ListaSimple getTablero(){ return tablero; }
    private Casilla getCasilla(int i){ return (Casilla) tablero.get(i); }

    private int indiceDe(Casilla obj) {
        int n = tablero.size();
        for (int i = 0; i < n; i++) if (getCasilla(i) == obj) return i;
        return -1;
    }
    //enum de resultados
    public enum Resultado {
        PRIMERA_SELECCION, DESELECCIONADA, CONCORDANCIA, SIN_CONCORDANCIA, INVALIDA
    }
}