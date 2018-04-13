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

/*
   Implémentation du DES simplifié.
   Pour plus de détails: https://www.cs.uri.edu/cryptography/dessimplified.htm
 */
public class SDESActivity extends AppCompatActivity {

    // Phase d'expansion: à partir d'un bloc de 6 bits on retourne 8 bits
    private final byte[] E =
            {
                    1, 2, 4, 3, 4, 3, 5, 6
            };

    /* Les 2 S-Box de SDES: un tableau à 3 dimensions contenant 2 tableaux à deux dimensions représentant les SBox.
     * Remarque: plutot que d'utiliser des tableaux à deux dimensions pour chaque S-Box on aurait pu aplatir les
     * données sur une seule ligne pour simplifier les calculs.
     * Par soucis de compréhension on garde la représentation originale de ces S-Box */
    private final byte[][][] SBOX =
            {       {
                            {0b101, 0b010, 0b001, 0b110, 0b011, 0b100, 0b111, 0b000},
                            {0b001, 0b100, 0b110, 0b010, 0b000, 0b111, 0b101, 0b011}
                    },
                    {
                            {0b100, 0b000, 0b110, 0b101, 0b111, 0b001, 0b011, 0b010},
                            {0b101, 0b011, 0b000, 0b111, 0b110, 0b010, 0b001, 0b100}
                    }
            };

    private EditText mEtInput; // le message hexadécimal sur 12 bits à coder / décoder
    private EditText mEtKey; // la clé héxa sur 9 bits
    private EditText mEtRondes; // le nombre de rondes du DES simplifié
    private TextView mTvRes;
    private Button mBtRun;
    boolean crypt = true; // valeur booleene égale à vrai si on code et faux si on decode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sdes);
        initView();
    }

    private void initView() {
        mEtInput = findViewById( R.id.etInput );
        mEtKey = findViewById( R.id.etKey);
        mEtRondes = findViewById( R.id.etRondes );
        mTvRes = findViewById( R.id.tvRes );
        mBtRun = findViewById( R.id.btRun );
        Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec);
        mSpinnerCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id ) {
                String selectedItem = parent.getItemAtPosition( position ).toString();
                String code = getString(R.string.code );
                String decode = getString(R.string.decode );
                if ( selectedItem.equals( code )) {
                    mBtRun.setText( code );
                    crypt = true;
                } else {
                    mBtRun.setText(decode );
                    crypt = false;
                }
            }

            @Override
            public void onNothingSelected( AdapterView<?> adapterView ) {}
        } );

        mBtRun.setOnClickListener(view -> {
            String input = mEtInput.getText().toString();
            String key = mEtKey.getText().toString();
            String strRondes = mEtRondes.getText().toString();
            if(strRondes.isEmpty()){
                Toast.makeText(this, R.string.round_missing, Toast.LENGTH_LONG).show();
            }
            int numRondes = Integer.parseInt(mEtRondes.getText().toString()); // on recupère le nombre de rondes à effectuer
            int hexInput;
            int hexKey;

            try{
                hexInput = parseHex(input); // on détermine la valeur hexa du nombre à code ou décoder
                hexKey = parseHex(key); // on détermine la valeur hexa de la clé
            } catch(NumberFormatException e){ // si le message ou la clé ne sont pas des valeurs hexadécimales on renvoie une erreur
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }
            int res = SDES(hexInput, hexKey, numRondes, crypt); // on execute SDES avec la clé et le message saisie par l'utilisateur

            String strRes = "0x"+ Integer.toHexString(res) + " = "+ Integer.toBinaryString(res);
            mTvRes.setText( strRes );
        });
    }

    // Fonction calculant la valeur héxadécimale d'une chaîne de caractère saisie par l'utilisateur
    private int parseHex(String s){
        if(s.length()>=2) { // on supprime les caractères "0x" ou "0X" s'ils existent en début de chaîne
            String firstTwoLetters = s.substring(0, 2);
            if (firstTwoLetters.equals("0X") || firstTwoLetters.equals("0x")) {
                s = s.substring(2);
            }
        }
        if(s.length() >3){ // quel que soit la taille du nombre saisi, on recupère seulement les 12 derniers bits
            s = s.substring(s.length()-3);
        }
        return Integer.parseInt(s, 16); // meme si la clé est sur plus de 9 bits, les bits en trop seront tronqués pendant la génération des sous-clés
    }

    /*
       Fonction implémentant l'algorithme SDES
       En entrée: int M = message hexadécimal sur 12 bits
                  int K = clé héxadécimale sur 9 bits
                  int totalRondes = le nombre de rondes à effectuer
                  boolean crypt = valeur booleene égale à vrai si on code et faux si on decode
       En sortie: La valeur héxadécimale du codage / décodage sur 12 bits
    */
    private int SDES(int M, int K, int totalRondes, boolean crypt){
        int[] splits = binarySplit(M, 2, 12); // on divise M en deux morceaux de 6 bits
        int L0 = splits[0]; // L0 est la partie gauche de M
        int R0 = splits[1]; // R0 est la partie droite de M

        for(int i=0;i<totalRondes;i++) { // pour chacune des rondes
            int subIndex = crypt ? i+1 :totalRondes-i; // si on code le message alors les clés sont calculées dans l'ordre croissant des rondes sinon dans l'ordre décroissant
            int subkey = getSubkey(K, subIndex); // on genere la sous clé pour cette ronde
            int PB = Feistel(R0, subkey); // on applique la fonction de Feistel à R0
            int R1 = PB ^ L0; // on effectue un XOR entre L0 et F(R0)
            L0 = R0; // la nouvelle partie gauche du message est égale à l'ancienne partie droite R0
            R0 = R1; // la nouvelle partie droite du message est égale à R1
        }
        M = (R0 << 6) | L0; // on reconstitue le message à partir des deux morceaux de 6 bits R0 et L0
        return M;
    }

    /* Fonction effectuant une permutation des bits d'un nombre en utilisant un tableau de permutation passé en paramètre
       En entrée: int input = un nombre binaire
                  byte[] posArr = un tableau de permutation
                  int size = le nombre de bits dans le nombre binaire
       En sortie: La permutation du nombre binaire "input"
    */
    private int permutation(int input, byte[] posArr, int size){
        int value = 0;
        for(byte pos: posArr){ // pour chaque valeur dans le tableau de permutation
            value = (value << 1) | getBitAt(input,pos,size); // on ajoute au resultat la valeur du bit situé à la position spécifié dans le tableau
        }
        return value;
    }

    /* Fonction permettant de recupérer la valeur du bit situé à la position "pos" dans un nombre binaire
       En entrée: int bin = un nombre binaire
                  int pos = la position du bit à recupérer
                  int size = le nombre de bits dans le nombre binaire
       En sortie: la valeur du nième bit du nombre binaire
     */
    private int getBitAt(int bin, int pos, int size){
        return ((bin >> (size-pos))&1); // on décale le nombre binaire pour que le nième bit soit le bit de poids faible puis on utilise le masque 1 pour le recupérer
    }

    /* Fonction permettant de scinder un nombre binaire en blocs de même taille
       En entrée: int bin = un nombre binaire
                  int totalSplit = le nombre de blocs que l'on cherche à obtenir
                  int binSize = le nombre de bits dans le nombre binaire
       En sortie: un tableau contenant chacun des blocs de bits
     */
    private int[] binarySplit(int bin, int totalSplit, int binSize){
        int splitSize = binSize/totalSplit; // le nombre de bits dans un bloc
        int[] splits = new int [totalSplit]; // tableau qui contiendra chacun des blocs
        int mask = (1 << splitSize)-1; // on utilise un masque contenant des 1 sur "splitSize" bits
        for(int i=totalSplit-1;i>=0;i--){ // pour chaque bloc en partant du dernier
            splits[i] = bin & mask; // on recupère la valeur du bloc avec le masque et on l'ajoute au tableau
            bin >>= splitSize; // on décale le nombre binaire d'autant de bits que contient un bloc
        }
        // si binSize % totalSplit != 0 alors les bits restant ne sont pas pris en compte
        return splits;
    }

    /* Fonction permettant d'effectuer une rotation des bits d'un nombre binaire (par défaut la rotation s'effectue sur la gauche)
       En entrée: int bin = un nombre binaire
                  int decal = la valeur du décalage pour la rotation. si le décalage est négatif la rotation est effectuée sur la droite
                  int size = le nombre de bits dans le nombre binaire
       En sortie: un nombre binaire dont les bits ont été décalés
    */
    private int circularShift(int bits, int decal, int size){
        int complement; // nombre de bits compris entre decal et size
        if(decal<0){ // si le décalage est négatif on inverse les valeurs du decalage et du complement
            complement = -decal;
            decal = size-complement;
        } else{
            complement = size-decal;
        }
        // on recupère les (size-decal) derniers bits avec un masque et on les decale ensuite vers la gauche
        // puis on ajoute les (decal) premiers bits à la fin du nombre binaire
        // Exemple avec une permutation à gauche de 1 sur le nombre binaire b = 1110 1100
        // on recupère les 7 derniers bits de b: 110 1100 qu'on décale de 1 sur la gauche: 1101 1000
        // puis on ajoute le premier bit de b sur le dernier bit du resultat: 1101 1001
        return ((bits & ((1 << complement)-1)) << decal) | (bits >>> complement);
    }

    /* Fonction de Feistel
       En entrée: int R0 = un nombre binaire sur 32 bits
                  int Subkey = la valeur de la sous-clé pour une rotation spécifique
       En sortie: Le résultat de la fonction de Feistel sur R0
    */
    private int Feistel(int R0, int Subkey){
        int ER0 = permutation(R0, E,6);
        int A = ER0 ^ Subkey;
        int[] aHalves = binarySplit(A, 2, 8); // on scinde A en 2 morceaux de 4 bits
        int B = 0;
        for(int i = 0; i<2; i++){ // pour les 2 S-BOX S1 et S2
            int Ai = aHalves[i];
            int row = (Ai & 8) >> 3; // x - - - => x
            int col = (Ai & 7); // - x y z => x y z
            int SiAi = SBOX[i][row][col]; // Si(Ai) sur 3 bits
            B = (B << 3) | SiAi; // concatenation des resultats des S-Box
        }
        System.out.println();
        return B;
    }

    /* Fonction generant une sous-clé pour une ronde spécifique
       En entrée: long key = la valeur de la clé sur 9 bits
                  int round = le numero de la ronde
       En sortie: la sous-clé correspondant à la ronde
    */
    private int getSubkey(int key, int round){ // une clé sur 9 bits
        int decal = (round-1)%9; // inutile de décaler les bits avec des valeurs > 9 puisque getSubKey(k, 1) = getSubkey(k, 10)
        return circularShift(key, decal, 9) >> 1; // on décale de (round-1) bits puis on garde les 8 premiers bits
    }
}
