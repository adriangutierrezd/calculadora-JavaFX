package com.example.calculadorafx;



import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalculadoraController {

    // Almacena la operación completa en forma de String
    private static String expression = "";
    // Almacena el último número o símbolo introducido por el usuario
    private static String lastChar = "";
    // Indica si ya se ha realizado una operación con anterioridad
    private static boolean prevOp = false;

    /* Guardará las 3 últimas operaciones para mostrarlas en el historial.
     * Se utiliza una cola porque la idea es acceder de forma recurrente al
     * primer elemento de esta, que al ser una cola, será la operación más
     * antigua, y por ende, la que hay que eliminar para introducir otra nueva. */
    private static Queue<String> lastExp = new LinkedList<String>();
    // Contador para saber cuantos elementos tenemos en el historial
    private static int counter;

    @FXML
    TextField resultContainer;
    @FXML
    VBox calculatorContainer;
    @FXML
    GridPane calculator;


    @FXML
    /**
     * Muestra el historial de operaciones recientes
     */
    protected void history(){
        counter = 0;
        // Quita los números de la calculadora
        calculatorContainer.getChildren().remove(0);
        calculatorContainer.setAlignment(Pos.TOP_RIGHT);

        // Creamos el botón que nos permitirá cerrar el historial
        Button exit = new Button("->");
        exit.getStyleClass().add("btnCalc");
        exit.setOnAction(Event ->{
            // Quitamos lo que haya en el historial
            calculatorContainer.getChildren().remove(0, counter + 1);
            // Volvemos a poner los números
            calculatorContainer.getChildren().add(calculator);
        });



        // Si no hay ninguna operación, lo indicamos
        if(lastExp.size() == 0){
            Label l = new Label("No hay operaciones");
            l.getStyleClass().add("historyResults");
            calculatorContainer.getChildren().add(counter, l);
            counter++;
        }

        // Iteramos las expresiones del historial
        for(String s : lastExp){
            Label l = new Label(s);
            l.getStyleClass().add("historyResults");
            calculatorContainer.getChildren().add(counter, l);
            counter++;
        }
        // Añadimos el botón de salir como último elemento
        calculatorContainer.getChildren().add(counter, exit);

    }



    @FXML
    /**
     * Añade un caracter tanto a la "pantalla" como a nuestra expresión matemática"
     */
    protected void addNumber(ActionEvent event){
        // ¿Se ha realizado alguna operación con anterioridad? Si es así, reseteamos la calculadora
        if(prevOp){ resetCalc(); }
        // Recoge el texto del botón que se ha pulsado.
        String text = ((Button)event.getSource()).getText();

        /* Generamos Pattern y Matcher para saber qué es
         * lo que ha introducido el usuario. */
        Pattern nums = Pattern.compile("\\d+");
        Matcher matchNums = nums.matcher(text);
        Matcher matchLastC = nums.matcher(lastChar);

        /* Esta expresión da true cuando se introducen 2 símbolos seguidos o si
         * el primer símbolo es un signo (para introducir un número negativo).
         *
         * Los símbolos en esta calculadora tienen un espacio por delante y por detrás.
         * De este modo, al introducir "-", realmente se está introduciendo " - ".
         *
         * En este caso, le retiramos los espacios. */
        if(!matchNums.matches() && !matchLastC.matches()){
            text = text.replace(" ", "");
        }

        // Añadimos el texto a la expresión
        expression += text;
        // Actualizamos la "pantalla"
        resultContainer.setText(expression);
        // Guardamos el último caracter introducido para poder comprobarlo más tarde
        lastChar = text;
    }


    /**
     * Guarda la expresión en la cola de últimas expresiones
     * @param expression Expresión matemática a almacenar
     */
    protected void saveExpression(String expression){
        /* Queremos mostrar solo 3 operaciones. Por ello, si la cola tiene un tamaño
         * inferior a 3, lo único que hacemos es añadir la expresión */
        if(lastExp.size() < 3){
            lastExp.offer(expression);
            return;
        }
        /* Si hay 3 expresiones, eliminamos la primera (la más antigua) e introducimos la nueva */
        lastExp.remove();
        lastExp.offer(expression);
    }

    @FXML
    /**
     * Llama al método que calcula el resultado y lo muestra en la "pantalla".
     */
    protected void showResult(){
        try{
            String result = String.valueOf(calculate(expression));
            resultContainer.setText(result);
            // Genereamos este String para poder ver la expresión y su resultado en el historial
            saveExpression(expression + " = " + result);
        }catch(ArithmeticException ae){
            resultContainer.setText("No se puede dividir entre 0");
        }catch (NumberFormatException nfe){
            resultContainer.setText("Operación no válida");
        }finally {
            /* Indicamos que se acaba de hacer una operación para que, al escribir un nuevo número, sepamos
             * que debemos reiniciar la calculadora antes de seguir. */
            prevOp = true;
        }
    }


    /**
     * Recibe una expresión matemática y devuelve su resultado
     * @param expr Expresión matemática
     * @return Resultadod e la expresión matemática
     * @throws ArithmeticException
     * @throws NumberFormatException
     */
    protected static int calculate(String expr) throws ArithmeticException, NumberFormatException{
        // Generamos la expresión necesaria para los cálculos
        expr = expr.replaceAll("\\+ ", "- -");
        expr = expr.replaceAll("--", "");

        /* Partimos la expresión por los símbolos de resta,
         * (los únicos que quedan que no son de multiplicación o
         * división) */
        String [] splArr = expr.split(" - ");

        /* Creamos una pila para ir guardando los resultados
         * de las sucesivas operaciones.
         * Utilizamos una pila porque vamos a acceder de forma
         * constante al último elemento de la colección, y las pilas
         * son el mejor tipo de dato para ello. */
        Stack<Integer> fResult = new Stack<>();

        for(String s : splArr){
            /* Creamos un array temporal en cada iteración
             * del bucle. En este array vamos a dividir cada
             * elemento del array splArr por los espacios, que
             * son lo que delimita cada expresión.
             *
             * Por ejemplo, en splArr podemos tener [3 / 3, 2 * 2, ...]
             * En ese caso, en la primera iteración, tmpArray sería: [3, /, 3] */
            String[] tmpArray = s.split(" ");
            int result;
            /* Si tmpArray.lenght es mayor a 1 quiere decir que en este
             * pequeño ladrillo de la expresión, hay una operación. */
            if(tmpArray.length > 1){
                int n1 = Integer.parseInt(tmpArray[0]);
                String o = tmpArray[1];
                int n2 = Integer.parseInt(tmpArray[2]);
                /* Sacamos los 2 números y su operador.
                 * Como ya hemos sustituidos los "+" por "-" y hemos
                 * dividido el string justo por este símbolo, nos quedan
                 * solo 2 opciones: multiplicación o división */
                if(o.equals("*")){
                    result = n1 * n2;
                }else{
                    result = n1 / n2;
                }
            }else{
                /* En este caso su lenght es < 1. Esto quiere
                 * decir que este ladrillo de la operación, es un
                 * solo número. Por ello, lo sacamos como resultado. */
                result = Integer.parseInt(tmpArray[0]);
            }

            // Si el resultado ya ha empezado a formarse...
            if(!fResult.empty()){
                /* Sacamos el último resultado disponible, le sumamos
                 * el resultado de la iteración y lo volvemos a meter. */
                int num = fResult.pop();
                /* En la pila volvemos a introducir el resultado, pero restándole el resultado (result)
                 * que ha dado la última operación.
                 *
                 * ¿Por qué restando? En nuestra expresión hemos cambiado todos los "+" por "-". De este modo,
                 * los resultados que antes eran positivos, aparecen en el resultado como negativos.
                 *
                 * Por ejemplo: 30 / 3 + 4 * 6 - 9 * 2 = 16
                 * Se vería como: 10 - - 24 - 18 = 16 */
                fResult.push(num - result);
            }else{
                // Si el resultado está vacío, metemos el primer número
                fResult.push(result);
            }
        }

        // Sacamos el último resultado (el resultado final).
        return fResult.pop();
    }

    @FXML
    /**
     * Elimina el contenido de la "pantalla" y resetea la calculadora
     */
    protected void deleteAll(){
        resultContainer.setText("");
        resetCalc();
    }

    @FXML
    /**
     * Elimina el último caracter introducido por el usuario
     */
    protected void deleteLast(){
        /* Dada la forma en la que estamos definiendo nuestra expresión,
         * el método para eliminar el último caracter debe hacer algunas
         * comprobaciones.
         *
         * Por ejemplo: Si la expresión en pantalla es: 3 / 3 * -2
         * Si el usuario quiere borrar hasta dejar la expresión como
         * 3 / 3, cuando borre el -2, se quedará por borrar el símbolo " * ",
         * que tiene espacios por delante y por detrás que hay que borrar
         * con un solo click para una buena experiencia de usuario y evitar errores. */

        if(expression.length() > 0){
            // Último caracter
            String lastCharExp = expression.substring(expression.length() - 1);
            if(lastCharExp.equals(" ")){
                /* Si es un espacio, es que el usuario quiere borrar un signo de operación, que
                 * se definen como " / ". Por ello, en lugar de borrar 1 caracter, borramos 3
                 * (los 2 espacios y el signo en sí). */
                expression = expression.substring(0, expression.length() - 3);
            }else{
                expression = expression.substring(0, expression.length() - 1);
            }
        }
        resultContainer.setText(expression);

    }

    /**
     * Resetea la calculadora para empezar las operaciones desde 0.
     */
    protected static void resetCalc(){
        expression = "";
        lastChar = "";
        prevOp = false;
    }


}