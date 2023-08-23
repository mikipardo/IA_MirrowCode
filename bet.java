package test;

public class Bet {
  public static void main(String[] args) {// fenix suns dallas maverick 152

    double cuarto = 5;// pon lo en minutos totales
    double puntos = 85;
    double puntosMax = 119.5;

    double resultado = 0;

    double porcenDiez = 10;

    double porcenQuince = 15;


    puntosMax = (puntosMax - puntos);

    puntosMax = (puntosMax / cuarto);

    puntosMax = (puntosMax * 20);

    porcenDiez = (puntosMax * porcenDiez) / 100;

    porcenQuince = (puntosMax * porcenQuince) / 100;
    
    System.err.println("Valor Inicial es de : " + puntosMax);

    resultado = (puntosMax - porcenQuince);

    System.out.println("Valor mínimo es de : " + resultado);

    resultado = (puntosMax - porcenDiez);

    System.out.println("Valor máximo es de : " + resultado);
  }
}
