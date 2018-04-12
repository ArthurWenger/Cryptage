package com.example.arthur.cryptage;

import java.util.HashMap;

/******************************************
 * Classe représentant un carré de polybe *
 ******************************************/
public class Polybe {
    private String key;
    private char replace;
    private HashMap<Character, int[]> map = null;
    private char[][] matrix = null;

    public Polybe(String key, char replace){
        this.key = parseKey(key);
        this.replace = replace;
    }

    /* Construction d'un tableau de caractère à deux dimensions représentant le carré de Polybe.
       On utilise pas la définition de Bibmath.net mais celle de Wikipedia: https://fr.wikipedia.org/wiki/Carr%C3%A9_de_Polybe#Avec_une_cl%C3%A9
       Une clé détermine les premières lettre du carré qui est ensuite rempli avec les lettres restantes de l'alphabet.
       Le tableau à deux dimension obtenu par cette méthode optimise l'accès à une lettre à partir de ses coordonnées dans le carré.
       Playfair manipulant les indices du carré de Polybe, cette méthode est publique */
    public char[][] matrixPolybe (){
        char poly_matrix[][]= new char[5][5];
        String s = stringPolybe();
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                poly_matrix[i][j] = s.charAt( i*5+j );
            }
        }
        return poly_matrix;
    }

    /* Plutot que d'utiliser une matrice à 2 dimensions pour représenter le carré de Polybe on peut
       également utiliser une HashMap associant à chaque lettre le couple (i,j) correspondant à ses
       coordonnées dans le carré (numéro de ligne et numéro de colonne). La recherche des coordonnées
       d'une lettre dans le carré sera donc nettement plus efficace. Par exemple, si la première ligne
       du carré de Polybe devrait être "a b c d e" alors le resultat de cette fonction contiendra:
       a -> {0,0}
       b -> {0,1}
       c -> {0,2}
       d -> {0,3}
       e -> {0,4}
     */
    private HashMap<Character, int[]> mapPolybe(){
        HashMap<Character, int[]> map = new HashMap<>();
        String s = stringPolybe();
        for(int i=0; i<5; i++){
            for(int j=0; j<5; j++){
                char c = s.charAt( i*5+j );
                map.put( c, new int[]{i,j});
            }
        }
        return map;
    }

    // Représentation du carré de Polybe sous forme de chaîne de caractères (lecture ligne par ligne)
    // Cette méthode sert uniquement à la construction d'un tableau ou d'une hashmap modélisant le carré
    private String stringPolybe(){
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        alphabet = alphabet.replace( String.valueOf(replace), "" );
        // la clé est obligatoirement de longueur <= 26 et contient uniquement des lettres uniques
        // gràce au formattage appliqué dans "mEtKey.setOnEditorActionListener" des différentes activités
        for(int i=0; i<key.length(); i++){ // on supprime chacune des lettres de la clé de l'alphabet usuel
            char c = key.charAt(i);
            alphabet = alphabet.replace( String.valueOf( c ),"" );
        }
        return key + alphabet; // on concatène la clé et les lettres de l'alphabet qui ne sont pas contenues dans la clé
    }

    // Méthode permettant d'obtenir le caractère situé aux coordonnées (row, col) du carré de Polybe
    public char getLetter(int row, int col){
        if(matrix == null){
            matrix = matrixPolybe(); // l'utilisation d'un tableau à deux dimensions optimise l'accès à la lettre
        }
        return matrix[row][col];
    }

    // Méthode permettant d'obtenir les coordonnées d'un caractère dans le carré de Polybe
    public int[] getCoords(char c){
        if(map == null){
            map = mapPolybe(); // l'utilisation d'une Hashmap optimise la récupération des coordonnées de la lettre
        }
        return map.get(c);
    }

    /* Fonction analysant un mot clé et supprimant les lettres qui sont présentes plusieurs fois
       Exemple: Hello => helo
       Cette méthode est "public static" car elle est utilisée pour analyser la clé saisie par l'utilisateur
       dans les différentes activités utilisant un carré de Polybe */
    public static String parseKey(String key){
        key = key.replaceAll("[^A-Za-z]+", "").toLowerCase();
        char c;
        StringBuilder res = new StringBuilder();
        for(int i=0; i<key.length(); i++){
            c = key.charAt(i);
            if(c != ' ')
                res.append(c);
            key = key.replace(c,' '); // On remplace toutes les occurences de la lettre par un espace
        }
        return res.toString();
    }
}
