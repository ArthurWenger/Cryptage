package com.example.arthur.cryptage;

/************************************************************************************************************************
 * Classe implémentant des fonctions de manipulation des chaînes de caractères très utilisées dans chacune des activité *
 ************************************************************************************************************************/
public class StringOperations {

    // Fonction renvoyant uniquement les lettres de l'alphabet contenus dans une chaîne de caractère
    // les lettres majuscules sont remplacées par des minuscules
    public static String getOnlyLetters(String s){
        return s.trim().toLowerCase().replaceAll("[^a-z]+", "");
    }

    // Fonction divisant une chaîne de caractères en morceaux de même taille
    // Si la longueur de la chaîne n'est pas un multiple du nombre de morceaux alors on complete la chaîne avec un caractère spécifié en paramètre
    public static String[] splitStringEqual(String text, int size, char fill){
        int textLength = text.length();
        int resLength = (textLength + size - 1) / size; // equivalent à Math.ceil(textLength%size)
        String[] res = new String[resLength];
        // alternativement on aurait pu utiliser le regex: text.split("(?<=\\G.{size})")
        for (int i = 0; i < resLength; i ++) {
            int start = i*size;
            res[i]= text.substring(start, Math.min(textLength, start + size));
        }
        int mod = textLength % size ;
        if(mod > 0){
            res[resLength-1] += repeatLetter(fill, size - mod);
        }
        return res;
    }

    // Fonction renvoyant une chaîne de caractère composée d'une lettre repetée n fois
    private static String repeatLetter(char c, int n){
        StringBuilder buff = new StringBuilder();
        for (int i = 0; i < n; i++) {
            buff.append(c);
        }
        return buff.toString();
    }
}
