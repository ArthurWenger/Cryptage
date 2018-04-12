package com.example.arthur.cryptage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;

public class DESActivity extends AppCompatActivity {

    // Permutation initiale du message
    private final byte[] IP =
            {
                    58, 50, 42, 34, 26, 18, 10, 2,
                    60, 52, 44, 36, 28, 20, 12, 4,
                    62, 54, 46, 38, 30, 22, 14, 6,
                    64, 56, 48, 40, 32, 24, 16, 8,
                    57, 49, 41, 33, 25, 17, 9, 1,
                    59, 51, 43, 35, 27, 19, 11, 3,
                    61, 53, 45, 37, 29, 21, 13, 5,
                    63, 55, 47, 39, 31, 23, 15, 7
            };

    // Permutation finale du message
    private final byte[] FP =
            {
                    40, 8, 48, 16, 56, 24, 64, 32,
                    39, 7, 47, 15, 55, 23, 63, 31,
                    38, 6, 46, 14, 54, 22, 62, 30,
                    37, 5, 45, 13, 53, 21, 61, 29,
                    36, 4, 44, 12, 52, 20, 60, 28,
                    35, 3, 43, 11, 51, 19, 59, 27,
                    34, 2, 42, 10, 50, 18, 58, 26,
                    33, 1, 41, 9, 49, 17, 57, 25
            };


    // Phase d'expansion: à partir d'un bloc de 32 bits on retourne 48 bits
    private final byte[] E =
            {
                    32, 1, 2, 3, 4, 5,
                    4, 5, 6, 7, 8, 9,
                    8, 9, 10, 11, 12, 13,
                    12, 13, 14, 15, 16, 17,
                    16, 17, 18, 19, 20, 21,
                    20, 21, 22, 23, 24, 25,
                    24, 25, 26, 27, 28, 29,
                    28, 29, 30, 31, 32, 1
            };

    // Permutation PC1 de la clé: à partir d'une clé de 64 bits on retourne 56 bits
    private final byte[] PC1 =
            {
                    57, 49, 41, 33, 25, 17, 9,
                    1,  58, 50, 42, 34, 26, 18,
                    10, 2,  59, 51, 43, 35, 27,
                    19, 11, 3,  60, 52, 44, 36,
                    63, 55, 47, 39, 31, 23, 15,
                    7,  62, 54, 46, 38, 30, 22,
                    14, 6,  61, 53, 45, 37, 29,
                    21, 13, 5, 28, 20, 12, 4
            };

    // Permutation PC2 pour le calcul des sous clés: à partir d'une clé de 56 bits on retourne 48 bits
    private final byte[] PC2 =
            {
                    14, 17, 11, 24, 1,  5,
                    3,  28, 15, 6,  21, 10,
                    23, 19, 12, 4,  26, 8,
                    16, 7,  27, 20, 13, 2,
                    41, 52, 31, 37, 47, 55,
                    30, 40, 51, 45, 33, 48,
                    44, 49, 39, 56, 34, 53,
                    46, 42, 50, 36, 29, 32
            };

    // Permutation P
    private final byte[] P =
            {
                    16, 7, 20, 21, 29, 12, 28, 17,
                    1, 15, 23, 26, 5, 18, 31, 10,
                    2, 8, 24, 14, 32, 27, 3, 9,
                    19, 13, 30, 6, 22, 11, 4, 25
            };

    // Ordre des rotations pour le calcul des sous clés
    private final byte[] subkeysShiftOrder =
            {
                    1, 1, 2, 2,
                    2, 2, 2, 2,
                    1, 2, 2, 2,
                    2, 2, 2, 1
            };

    /* Les 8 S-Box du DES: un tableau à 3 dimensions contenant 8 tableaux à deux dimensions représentant les SBox.
     *  Remarque: plutot que d'utiliser des tableaux à deux dimensions pour chaque S-Box on aurait pu aplatir les
     *  données sur une seule ligne pour simplifier les calculs.
     *  Par soucis de compréhension on garde la représentation originale de ces S-Box */
    private final byte[][][] SBOX =
            {       {
                            { 14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7 },
                            { 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8 },
                            { 4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0 },
                            { 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13 }
                    },
                    {
                            { 15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10 },
                            { 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5 },
                            { 0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15 },
                            { 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9 }
                    },
                    {
                            { 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8 },
                            { 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1 },
                            { 13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7 },
                            { 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12 }
                    },
                    {
                            { 7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15 },
                            { 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9 },
                            { 10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4 },
                            { 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14 }
                    },
                    {
                            { 2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9 },
                            { 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6 },
                            { 4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14 },
                            { 11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3 }
                    },
                    {
                            { 12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11 },
                            { 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8 },
                            { 9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6 },
                            { 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13 }
                    },
                    {
                            { 4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1 },
                            { 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6 },
                            { 1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2 },
                            { 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12 }
                    },
                    {
                            { 13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7 },
                            { 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2 },
                            { 7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8 },
                            { 2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}
                    }
            };

    private EditText mEtInput; // message à coder ou à décoder en hexa sur 64 bits
    private EditText mEtKey; // clé en hexa sur 64 bits
    private TextView mTvRes; // resultat du cryptage ou du décryptage
    private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_des);
        initView();
    }

    private void initView() {
        mEtInput = findViewById( R.id.etInput );
        mEtKey = findViewById( R.id.etKey);
        mTvRes = findViewById( R.id.tvRes );
        mBtRun = findViewById( R.id.btRun );
        Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec); // option permettant de selectionner le codage ou le decodage du message
        mSpinnerCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
                String selectedItem = parent.getItemAtPosition( position ).toString();
                if ( selectedItem.equals( "Coder" ) ) {
                    mBtRun.setText( "Coder" );
                } else {
                    mBtRun.setText( "Décoder" );
                }
            }

            @Override
            public void onNothingSelected( AdapterView<?> adapterView ) {}
        } );

        mBtRun.setOnClickListener(view -> {
            String input = mEtInput.getText().toString();
            String codec = mBtRun.getText().toString();
            String key = mEtKey.getText().toString();
            long hexInput;
            long hexKey;

            try{
                hexInput = parseHex(input); // on détermine la valeur hexadécimale du message
                hexKey = parseHex(key); // on détermine la valeur hexadécimale de la clé
            } catch(NumberFormatException e){ // si le message ou la clé ne sont pas des valeurs hexadécimales on renvoie une erreur
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            boolean crypt = !codec.equals("Décoder"); // valeur booleene égale à vrai si on code et faux si on decode
            long res = DES(hexInput, hexKey, crypt); // on execute DES avec la clé et le message saisie par l'utilisateur

            String strRes = "0x"+Long.toHexString(res) + " = "+ Long.toBinaryString(res);
            mTvRes.setText( strRes );
        });
    }

    // Fonction calculant la valeur héxadécimale d'une chaîne de caractère saisie par l'utilisateur
    private long parseHex(String s){
        if(s.length()>=2) { // on supprime les caractères "0x" ou "0X" s'ils existent en début de chaîne
            String firstTwoLetters = s.substring(0, 2);
            if (firstTwoLetters.equals("0X") || firstTwoLetters.equals("0x")) {
                s = s.substring(2);
            }
        }
        if(s.length() >=16){ // si la longueur de la chaîne est supérieure à 16 alors le message contient au moins 4*16 = 64 bits
            return new BigInteger(s, 16).longValue(); // dans ce cas on ne garde que les 64 derniers bits du message
        } else{ // si le message contient moins de 64 bits
            return Long.parseLong(s,16); // le type "long" suffit à contenir tout le message
        }
    }

    /*
       Fonction implémentant l'algorithme DES
       En entrée: long M = message hexadécimal sur 64 bits
                  long K = clé héxadécimale sur 64 bits
                  boolean crypt = valeur booleene égale à vrai si on code et faux si on decode
       En sortie: La valeur héxadécimale du codage / décodage sur 64 bits
    */
    private long DES(long M, long K, boolean crypt){
        long[] subkeys = genSubkeys(K); // on genère les 16 sous-clés qu'on stocke dans un tableau
        M = permutation(M, IP,64); // on applique la permutation initiale sur le message
        // on divise M en deux morceaux de 32 bits
        long[] splits = binarySplit(M, 2, 64);
        long L0 = splits[0]; // L0 est la partie gauche de M
        long R0 = splits[1]; // R0 est la partie droite de M

        for(int i=0;i<16;i++) { // pour chacune des 16 rondes
            int subIndex = crypt ? i : 15-i; // si on code le message alors les clés sont prises dans l'ordre croissant des indices sinon elles sont prises dans l'ordre décroissant
            long PB = Feistel(R0, subkeys[subIndex]); // on applique la fonction de Feistel à R0
            long R1 = PB ^ L0; // on effectue un XOR entre L0 et F(R0)
            L0 = R0; // la nouvelle partie gauche du message est égale à l'ancienne partie droite R0
            R0 = R1; // la nouvelle partie droite du message est égale à R1
        }
        M = (R0 << 32) | L0; // on reconstitue le message à partir des deux morceaux de 32 bits R0 et L0
        return permutation(M, FP,64); // on applique la permutation finale sur le message
    }

    /* Fonction effectuant une permutation des bits d'un nombre en utilisant un tableau de permutation passé en paramètre
       En entrée: long input = un nombre binaire
                  byte[] posArr = un tableau de permutation
                  int size = le nombre de bits dans le nombre binaire
       En sortie: La permutation du nombre binaire "input"
     */
    private long permutation(long bin, byte[] posArr, int size){
        long value = 0;
        for(byte pos: posArr){ // pour chaque valeur dans le tableau de permutation
            value = (value << 1) | getBitAt(bin,pos,size);  // on ajoute au resultat la valeur du bit situé à la position spécifié dans le tableau
        }
        return value;
    }

    /* Fonction permettant de recupérer la valeur du bit situé à la position "pos" dans un nombre binaire
       En entrée: long bin = un nombre binaire
                  int pos = la position du bit à recupérer
                  int size = le nombre de bits dans le nombre binaire
       En sortie: la valeur du nième bit du nombre binaire
     */
    private long getBitAt(long bin, int pos, int size){
        return (bin >> (size-pos))&1; // on décale le nombre binaire pour que le nième bit soit le bit de poids faible puis on utilise le masque 1 pour le recupérer
    }

    /* Fonction permettant de scinder un nombre binaire en blocs de même taille
       En entrée: long bin = un nombre binaire
                  int totalSplit = le nombre de blocs que l'on cherche à obtenir
                  int binSize = le nombre de bits dans le nombre binaire
       En sortie: un tableau contenant chacun des blocs de bits
     */
    private long[] binarySplit(long bin, int totalSplit, int binSize){
        int splitSize = binSize/totalSplit; // le nombre de bits dans un bloc
        long[] splits = new long [totalSplit]; // tableau qui contiendra chacun des blocs
        long mask = (1L << splitSize)-1; // on utilise un masque contenant des 1 sur "splitSize" bits
        for(int i=totalSplit-1;i>=0;i--){ // pour chaque bloc en partant du dernier
            splits[i] = bin & mask; // on recupère la valeur du bloc avec le masque et on l'ajoute au tableau
            bin >>= splitSize; // on décale le nombre binaire d'autant de bits que contient un bloc
        }
        // Remarque: si binSize % totalSplit != 0 alors les bits restant ne sont pas pris en compte
        return splits;
    }

    /* Fonction permettant d'effectuer une rotation des bits d'un nombre binaire (par défaut la rotation s'effectue sur la gauche)
       En entrée: long bin = un nombre binaire
                  int decal = la valeur du décalage pour la rotation. si le décalage est négatif la rotation est effectuée sur la droite
                  int size = le nombre de bits dans le nombre binaire
       En sortie: un nombre binaire dont les bits ont été décalés
     */
    private long circularShift(long bin, int decal, int size){
        int complement; // nombre de bits compris entre decal et size
        if(decal<0){ // si le décalage est négatif on inverse les valeurs du decalage et du complement
            complement = -decal;
            decal = size-complement;
        } else{
            complement = size-decal;
        }
        /* On recupère les (size-decal) derniers bits avec un masque et on les decale ensuite vers la gauche
           Puis on ajoute les (decal) premiers bits à la fin du nombre binaire
           Exemple: avec une permutation à gauche de 1 sur le nombre binaire b = 1110 1100
           on recupère les 7 derniers bits de b: 110 1100 qu'on décale de 1 sur la gauche: 1101 1000
           puis on ajoute le premier bit de b sur le dernier bit du resultat: 1101 1001 */
        return ((bin & ((1 << complement)-1)) << decal) | (bin >>> complement);
    }

    /* Fonction de Feistel
       En entrée: long R0 = un nombre binaire sur 32 bits
                  long Subkey = la valeur de la sous-clé pour une rotation spécifique
       En sortie: Le résultat de la fonction de Feistel sur R0
    */
    private long Feistel(long R0, long Subkey){
        long ER0 = permutation(R0, E,32); // on applique l'expansion E sur R0
        long A = ER0 ^ Subkey; // on effectue un XOR entre E(R0) et la sous-clé
        long[] aSplits = binarySplit(A, 8, 48); // on scinde A en 8 morceaux de 6 bits
        long B = 0;
        for(int i = 0; i<8; i++){ // pour chaque bloc de 8 bits on applique la S-Box adéquate
            int Ai = (int) aSplits[i];
            int row = ((Ai & 32) >> 4) | (Ai & 1 ); // On recupère le premier et le dernier bit du bloc: x - - - - y => x y
            int col = (Ai & 31 ) >> 1; // On recupère les 4 bits au centre du bloc: - x y z t - => x y z t
            int SiAi = SBOX[i][row][col]; // on détermine la valeur de la S-Box i sur le bloc i
            B = (B << 4) | SiAi; // on concatene les resultats des S-Box dans la variable B
        }
        return permutation(B, P,32); // on applique la permutation P sur B
    }

    /* Fonction generant les 16 sous-clés du DES en une seule fois
       En entrée: long key = la valeur de la clé sur 64 bits
       En sortie: un tableau contenant les 16 sous-clés
    */
    private long[] genSubkeys(long key){
        long[] subkeys = new long[16];
        key = permutation(key, PC1,64); // on applique la permutation PC1 sur la clé

        for(int i=0;i<16;i++){ // pour chacune des 16 rondes
            long[] kHalves = binarySplit(key,2,56); // on divise la clé en deux morceaux de 28 bits
            int decal = subkeysShiftOrder[i]; // on recupère la valeur de la rotation pour cette ronde
            key = circularShift(kHalves[0], decal,28) << 28 | circularShift(kHalves[1], decal,28); // on applique la rotation sur les deux morceaux de 28 bits
            subkeys[i] = permutation(key, PC2,56); // on applique la permutation PC2 sur la clé pour obtenir la sous-clé de cette ronde
        }
        return subkeys;
    }
}
