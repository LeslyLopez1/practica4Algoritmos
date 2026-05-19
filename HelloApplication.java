package com.example.practica4listas;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class HelloApplication extends Application {

    private Juego juego;
    private int filas = 4;
    private int columnas = 10;

    private Map<Casilla, Button> casillaABoton = new HashMap<>();

    private Label lblEncontradas;
    private Label lblPendientes;
    private Label lblEstado;
    private GridPane cuadricula;
    private VBox ventana;
    private Button botonPista1;
    private Button botonPista2;

    @Override
    public void start(Stage escenario) {
        mostrarDialogoConfiguracion(escenario);
    }

    private void mostrarDialogoConfiguracion(Stage escenarioPrincipal) {
        Stage dialogo = new Stage();
        dialogo.setTitle("Nuevo juego");

        Label lbFilas = new Label("Renglones:");
        Label lbCols  = new Label("Columnas:");

        Spinner<Integer> spFilas = new Spinner<>(4, 20, 4);
        Spinner<Integer> spCols  = new Spinner<>(10, 20, 10);
        spFilas.setEditable(true);
        spCols .setEditable(true);

        Button btnIniciar = new Button("Iniciar");
        btnIniciar.setOnAction(e -> {
            filas    = spFilas.getValue();
            columnas = spCols.getValue();
            dialogo.close();
            iniciarJuego(escenarioPrincipal);
        });

        GridPane formulario = new GridPane();
        formulario.setHgap(8);
        formulario.setVgap(8);
        formulario.setPadding(new Insets(16));
        formulario.add(lbFilas,0, 0);
        formulario.add(spFilas,1, 0);
        formulario.add(lbCols,0, 1);
        formulario.add(spCols,1, 1);
        formulario.add(btnIniciar,0, 2, 2, 1);
        GridPane.setHalignment(btnIniciar, javafx.geometry.HPos.CENTER);

        dialogo.setScene(new Scene(formulario, 260, 150));
        dialogo.showAndWait();
    }


    private void iniciarJuego(Stage escenario) {
        juego = new Juego(filas, columnas);
        casillaABoton.clear();
        ventana = construirVentana();
        construirCuadricula();
        actualizarContadores();

        escenario.setScene(new Scene(ventana,columnas * 58 + 160, filas * 58 + 160));
        escenario.setTitle("Number Match");
        escenario.show();
    }

    private VBox construirVentana() {
        VBox vbox = new VBox(8);
        vbox.setPadding(new Insets(12));

        Label titulo = new Label("Number Match");
        titulo.setFont(Font.font("SansSerif", FontWeight.BOLD, 18));

        lblEncontradas = new Label("Encontradas: 0");
        lblPendientes  = new Label("Pendientes: 0");
        HBox contadorBox = new HBox(16, lblEncontradas, lblPendientes);

        Button btnPista    = new Button("Pista");
        Button btnDeshacer = new Button("Deshacer");
        Button btnNuevo    = new Button("Nuevo juego");

        btnPista   .setOnAction(e -> mostrarPista());
        btnDeshacer.setOnAction(e -> deshacerMovimiento());
        btnNuevo   .setOnAction(e -> mostrarDialogoConfiguracion(
                (Stage) ventana.getScene().getWindow()));

        VBox panelBotones = new VBox(4, btnPista, btnDeshacer, btnNuevo);

        cuadricula = new GridPane();
        cuadricula.setHgap(3);
        cuadricula.setVgap(3);

        ScrollPane scrollTablero = new ScrollPane(cuadricula);
        scrollTablero.setFitToWidth(true);

        HBox panelPrincipal = new HBox(12, panelBotones, scrollTablero);

        lblEstado = new Label(" ");

        vbox.getChildren().addAll(titulo, contadorBox, panelPrincipal, lblEstado);
        return vbox;
    }

    private void construirCuadricula() {
        cuadricula.getChildren().clear();
        casillaABoton.clear();

        ListaSimple tablero = juego.getTablero();
        int n = tablero.size();
        int c = juego.getColumnas();

        for (int i = 0; i < n; i++) {
            Casilla casilla = tablero.get(i);
            Button  boton   = crearBotonCasilla(casilla);
            casillaABoton.put(casilla, boton);
            cuadricula.add(boton, i % c, i / c);
        }
    }

    private Button crearBotonCasilla(Casilla casilla) {
        Button boton = new Button(String.valueOf(casilla.getNumero()));
        boton.setPrefSize(50, 50);
        boton.setFont(Font.font("SansSerif", FontWeight.BOLD, 14));

        boton.setOnAction(e -> {
            if (casilla.isEliminada()) return;
            limpiarResaltadoPista();
            Juego.Resultado resultado = juego.seleccionar(casilla);
            procesarResultado(resultado, casilla);
        });
        return boton;
    }

    private void procesarResultado(Juego.Resultado resultado, Casilla casilla) {
        switch (resultado) {
            case PRIMERA_SELECCION:
                quitarSeleccionActivas();
                Button b = casillaABoton.get(casilla);
                if (b != null) b.setStyle("-fx-background-color: yellow;");
                lblEstado.setText("Seleccionada " + casilla.getNumero() + ". Elige otra.");
                break;

            case DESELECCIONADA:
                quitarSeleccionActivas();
                lblEstado.setText("Casilla deseleccionada.");
                break;

            case CONCORDANCIA:
                deshabilitarCasillasEliminadas();
                quitarSeleccionActivas();
                actualizarContadores();
                lblEstado.setText("Concordancia encontrada. Total: " + juego.getConcordanciasEncontradas());
                if      (juego.todasEliminadas()) lblEstado.setText("Ganaste!");
                else if (juego.sinMovimientos())  lblEstado.setText("Sin mas movimientos.");
                break;

            case SIN_CONCORDANCIA:
                quitarSeleccionActivas();
                Button seleccionado = casillaABoton.get(juego.getSeleccionada());
                if (seleccionado != null) seleccionado.setStyle("-fx-background-color: yellow;");
                lblEstado.setText("Sin concordancia. Selecciona otra casilla.");
                break;

            default:
                lblEstado.setText("Casilla invalida.");
        }
    }

    private void deshabilitarCasillasEliminadas() {
        for (Map.Entry<Casilla, Button> entrada : casillaABoton.entrySet()) {
            if (entrada.getKey().isEliminada()) {
                Button boton = entrada.getValue();
                boton.setDisable(true);
                boton.setStyle("-fx-opacity: 1.0; -fx-text-fill: lightgray;");
            }
        }
    }

    private void mostrarPista() {
        limpiarResaltadoPista();
        Casilla[] pista = juego.getPista();
        if (pista == null) { lblEstado.setText("No hay pistas disponibles."); return; }
        botonPista1 = casillaABoton.get(pista[0]);
        botonPista2 = casillaABoton.get(pista[1]);
        if (botonPista1 != null) botonPista1.setStyle("-fx-background-color: lightblue;");
        if (botonPista2 != null) botonPista2.setStyle("-fx-background-color: lightblue;");
        lblEstado.setText("Pista: " + pista[0].getNumero() + " y " + pista[1].getNumero());
    }

    private void limpiarResaltadoPista() {
        if (botonPista1 != null) { botonPista1.setStyle(""); botonPista1 = null; }
        if (botonPista2 != null) { botonPista2.setStyle(""); botonPista2 = null; }
    }

    private void deshacerMovimiento() {
        limpiarResaltadoPista();
        Casilla[] par = juego.deshacer();
        if (par == null) { lblEstado.setText("Nada que deshacer."); return; }
        Button b0 = casillaABoton.get(par[0]);
        Button b1 = casillaABoton.get(par[1]);
        if (b0 != null) { b0.setDisable(false); b0.setStyle(""); }
        if (b1 != null) { b1.setDisable(false); b1.setStyle(""); }
        quitarSeleccionActivas();
        actualizarContadores();
        lblEstado.setText("Movimiento deshecho.");
    }

    private void quitarSeleccionActivas() {
        for (Map.Entry<Casilla, Button> entrada : casillaABoton.entrySet()) {
            if (!entrada.getKey().isEliminada()) entrada.getValue().setStyle("");
        }
    }

    private void actualizarContadores() {
        lblEncontradas.setText("Encontradas: " + juego.getConcordanciasEncontradas());
        lblPendientes .setText("Pendientes: "  + juego.getConcordanciasPendientes());
    }

    public static void main(String[] args) { launch(args); }
}