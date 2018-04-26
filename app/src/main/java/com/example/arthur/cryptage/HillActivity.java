package com.example.arthur.cryptage;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HillActivity extends AppCompatActivity {
    private EditText mEtInput; // message à coder ou à décoder
    private EditText mEtMatSize; // taille de la matrice
    private TextView mTvRes; // resultat du cryptage ou du décryptage
    private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
    TableLayout table; // table contenant un ensemble de champs de texte sous la forme d'un carré représentant la matrice
    float scale;
    boolean crypt = true; // valeur booleene égale à vrai si on code et faux si on decode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hill);
        initView();
    }

    private void initView() {
        scale = this.getResources().getDisplayMetrics().density; // correspondance entre un pixel et un dp: permet d'adapter la taille de la table en fonctiond e la taille de la matrice
        mEtInput = findViewById( R.id.etInput );
        mEtMatSize = findViewById( R.id.etKey);
        mTvRes = findViewById( R.id.tvRes );
        mBtRun = findViewById( R.id.btRun );
        table= findViewById(R.id.tableLayout);
        Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec); // option permettant de selectionner le codage ou le decodage du message
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

        mEtMatSize.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String strSize = mEtMatSize.getText().toString();
                if(!strSize.isEmpty()) { // si le champs contenant la taille de la matrice est n'est pas vide
                    int size = Integer.parseInt(strSize);
                    fillTable(size, table); // on rempli la table avec autant d'EditText que la matrice contient de valeurs
                }
            }
            return false;
        });

        mBtRun.setOnClickListener(view -> {
            String input = StringOperations.getOnlyLetters(mEtInput.getText().toString());
            if(input.isEmpty()){ // si le message est vide on affiche une erreur
                Toast.makeText(this, R.string.input_missing_word, Toast.LENGTH_LONG).show();
                return;
            }
            String strSize = mEtMatSize.getText().toString();
            if(strSize.isEmpty()) { // si le champs contenant la taille de la matrice est n'est pas vide
                Toast.makeText(this, R.string.matrix_size_missing, Toast.LENGTH_LONG).show();
                return;
            }
            int size = Integer.parseInt(strSize);
            if(size == 0) { // si la taille de la matrice est 0 on affiche une erreur
                Toast.makeText(this, R.string.matrix_size_0, Toast.LENGTH_LONG).show();
                return;
            }
            int[][] A;
            try{ A = getKeyMatrix(size); } // on recupère la matrice à partir de la table contenant les EditText
            catch (NumberFormatException e){ // s'il manque des valeurs dans la matrice on affiche une erreur
                Toast.makeText(this, R.string.matrix_value_missing, Toast.LENGTH_LONG).show();
                return;
            }
            try {
                mTvRes.setText( Hill(input, A, crypt) ); // on décode ou on code le message avec l'algorithme de Hill
            } catch (Exception e) {
                // si la matrice saisie n'est pas carrée ou n'est pas inversible on affiche un message d'erreur
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /* Fonction permettant de remplir une table avec des EditText pour saisir la matrice utilisée par l'algorithme de Hill
       En entrée: int n = la taille de la matrice
                  TableLayout table = la table représentant la matrice
     */
    private void fillTable(int n, TableLayout table) {
        int dp = n < 8 ? 40 : 30;
        int pixels = (int) (dp * scale + 0.5f);
        table.removeAllViews();
        for (int i = 0; i < n; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < n; j++) {
                EditText edit = new EditText(this);
                edit.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
                edit.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                edit.setMinWidth(pixels);
                edit.setFilters( new InputFilter[] { new InputFilter.LengthFilter(4) } );
                row.addView(edit);
            }
            table.addView(row);
        }
    }

    /* Fonction permettant de transformer le contenu d'une table contenant des EditText en tableau à 2 dimensions représentant une matrice
       En entrée: int size = la taille de la matrice
       En sortie: un tableau à 2 dimensions représentant la matrice
     */
    private int[][] getKeyMatrix(int size) throws NumberFormatException {
        int[][] res = new int[size][size];
        for(int i=0;i<size;i++){ // pour chaque ligne de la table
            TableRow row = (TableRow)table.getChildAt(i);
            for(int j=0;j<size;j++){ // pour chaque element de la ligne
                String number = ((EditText)(row.getChildAt(j))).getText().toString();
                // on essaie de transformer la valeur contenue dans un EditText en entier
                res[i][j]= Integer.parseInt(number); // si l'EditText ne contient rien une erreur sera renvoyée
            }
        }
        return res;
    }

    /*
	   Fonction de cryptage ou de décryptage utilisant l'algorithme de Hill
	   En entrée: String input = message à coder / décoder
	              int[][] A = la matrice saisie par l'utilisateur
	              boolean crypt = valeur booleenne égale à vrai si on code et faux si on décode
	   En sortie: Une chaine de caractère représentant le codage ou le décodage du message
	*/
    private String Hill(String input, int[][] A, boolean crypt) throws Exception{
        if(!isInvertible(A)){ // si la matrice n'est pas inversible on renvoie une erreur
            throw new Exception(getString(R.string.matrix_not_invertible));
        } else if(!isSquare(A)){ // si la matrice n'est pas carrée on renvoie une erreur (cas impossible normalement ?)
            throw new Exception(getString(R.string.matrix_not_square));
        }
        StringBuilder sb = new StringBuilder(input.length());
        int matSize = A.length;
        String[] splits = StringOperations.splitStringEqual(input, matSize, 'x'); // on divise le message en morceaux de longueur égale à la taille de la matrice
        A = crypt ? A : inverse(A); // si on code le message on utilise la matrice saisie par l'utilisateur sinon on utilise la matrice inverse

        for(String bloc:splits){ // pour chaque morceau composant le message
            int[] vector = new int[matSize]; // vecteur contenant la position de chacune des lettres dans l'alphabet [0:25]
            for(int i=0; i<matSize; i++){ // pour chaque lettre dans le morceau
                vector[i] = bloc.charAt(i) - 97; // on ajoute sa position dans le vecteur
            }
            int[] multAV = multMatrixVector(A, vector); // tableau contenant le resultat des combinaisons linéaires A*vector
            for(int value: multAV){ // on traduit le vecteur resultat en chaîne de caractère
                sb.append((char)(value + 97)); // pour chaque nombre on associe la lettre correspondante dans l'alphabet avec la table ASCII
            }
        }
        return sb.toString();
    }

    // le modulo '%' en java peut être négatif
    private int trueMod(int n, int mod){
        int res = n%mod;
        return res >= 0 ? res : res + mod;
    }

    /* Fonction implémentant l'algorithme d'euclide étendu
       En entrée: int a, int b: un couple d'entier
       En sortie: le couple u et v tel que au + bv = pgcd(a,b)
     */
    private int[] euclideEtendu(int a, int b){
        int invU=1, invV=1;  // variable inversant le signe final de u et v en fonction du signe des arguments a et b
        if(a<0){ // si les arguments sont negatifs: |a|*(signe de a)*u + |b|*(signe b)*v = r
            a=-a;
            invU=-1;
        }
        if(b<0){
            b=-b;
            invV=-1;
        }

        int u = 1, v = 0, r = a, rPrime = b, uPrime = 0, vPrime = 1;
        int q, us, rs, vs; // variables intermédiaires

        while(rPrime != 0){
            q = r/rPrime;
            rs = r; us = u; vs = v;
            r = rPrime; u = uPrime; v = vPrime;
            rPrime = rs - q*rPrime;
            uPrime = us - q*uPrime;
            vPrime = vs - q*vPrime;
        }
        u*=invU ; v*=invV;
        return new int[]{u,v};
    }

    /***************************************************
     * OPERATIONS SUR LES MATRICES CARREES DE TAILLE N *
     ***************************************************/

    // Fonction vérifiant si une matrice est carrée
    private boolean isSquare(int[][] mat){
        return mat.length == mat[0].length;
    }

    // Fonction vérifiant si une matrice est inversible dans Z/26Z
    private boolean isInvertible(int[][] mat) {
        int det = determinant(mat);
        return det % 2 != 0 && det % 13 != 0; // si le déterminant est divisible par 2 ou 13 alors la matrice n'est pas inversible
    }

    // Fonction calculant l'inverse d'une matrice passée en paramètre
    private int[][] inverse(int[][] mat){
        return multMatrixConst(transpose(cofactor(mat)), determinantInverse(mat)); // A^-1 = t(C(A)) * det(A)^-1
    }

    // Fonction calculant le determinant inverse dans Z/26Z d'une matrice passée en paramètre
    private int determinantInverse(int[][] mat) {
        // il existe u et v tels que: det * u + 26 * v = 1
        // donc det * u = 1 mod 26 , u correspond au determinant inverse de la matrice
        return euclideEtendu(determinant(mat), 26)[0];
    }

    // Fonction calculant le determinant d'une matrice
    private int determinant(int[][] mat){
        int matSize = mat.length;
        if (matSize == 1) { // si la matrice contient un seul element on renvoie directement le resultat
            return mat[0][0];
        }
        if (matSize == 2) { // si la matrice est de taille 2 on renvoie directement a*d - b*c
            return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
        }
        int sum = 0;
        for (int i=0; i<matSize; i++) { // pour chaque valeur dans la première ligne de la matrice
            // on calcule le determinant de la sous-matrice qui ne contient ni la première ligne de A ni la colonne de la valeur courante
            // les appels recursifs à cette fonction s'arreteront quand le taille de la sous matrice sera inferieur ou égale à 2
            // le determinant de A est calculé avec la somme suivante:
            sum += getSign(i) * mat[0][i] * determinant(createSubMatrix(mat, 0, i));
        }
        return sum;
    }

    // Fonction calculant le signe de la somme de chaque multiplication de sous matrice pour le calcul du determinant
    private int getSign(int i){
        return (i & 1) == 0 ? 1 : -1; // si l'indice de la première ligne est pair on additionne le determinant de la sous matrice sinon on le soustrait
    }

    /* Fonction calculant une sous-matrice en supprimant une ligne et une colonne d'une matrice passée en paramètre
       En entrée: int[][] mat = la matrice dont on cherche à créer une sous matrice
                  int excludeRow = le numéro de la ligne à exclure
                  int excludeCol = le numéro de la colonne à exclure
       En sortie: un tableau à deux dimensions contenant la sous matrice
     */
    private int[][] createSubMatrix(int[][] mat, int excludeRow, int excludeCol) {
        int matSize = mat.length;
        int[][] res = new int[matSize-1][matSize-1]; // tableau représentant la sous matrice
        int r = -1; // indice pour les lignes de la sous matrice
        for (int i=0;i<matSize;i++) { // pour chaque ligne de la matrice originale
            if (i==excludeRow) // si la ligne correspond à la ligne à exclure ou passe à l'itération suivante
                continue;
            r++; // on incremente l'indice des lignes de la sous matrice
            int c = -1; // indice des colonnes de la sous matrice
            for (int j=0;j<matSize;j++) { // pour chaque colonne de la matrice originale
                if (j==excludeCol) // si la colonne correspond à la colonne à exclure ou passe à l'itération suivante
                    continue;
                res[r][++c] = mat[i][j]; // on recopie la valeur de la matrice originale dans la sous matrice et on incremente le numéro de colonne de la sous matrice
            }
        }
        return res;
    }

    // Fonction determinant la matrice de cofacteurs c'est à dire la comatrice d'une matrice fournie en paramètre
    private int[][] cofactor(int[][] mat){
        int matSize = mat.length;
        int[][] res = new int[matSize][matSize];
        for (int i=0;i<matSize;i++) { // pour chaque ligne de la matrice
            for (int j=0; j<matSize;j++) { // pour chaque colonne de la matrice
                res[i][j] = getSign(i) * getSign(j) * determinant(createSubMatrix(mat, i, j)); // on ajoute le cofacteur de la valeur courante au resultat
            }
        }
        return res;
    }

    // Fonction effectuant une transposition d'une matrice fournie en paramètre
    private int[][] transpose(int[][] mat) {
        int matSize = mat.length;
        int[][] res = new int[matSize][matSize];
        for (int i=0;i<matSize;i++) { // pour chaque ligne de la matrice
            for (int j=0;j<matSize;j++) { // pour chaque colonne de la matrice
                res[j][i] = mat[i][j]; // on inverse le numero de ligne et de colonne pour remplir la transposée
            }
        }
        return res;
    }

    // Fonction effectuant la multiplication d'une matrice carrée A par un vecteur V de même taille (dans Z/26Z)
    private int[] multMatrixVector(int[][] A, int[] V){
        int sizeA = A.length;
        int[] res = new int[sizeA];
        for(int i=0;i<sizeA;i++){ // pour chaque ligne de la matrice
            int sum = 0;
            for(int j=0;j<sizeA;j++){ // pour chaque colonne de la matrice
                sum += A[i][j] * V[j]; // on calcule les combinaisons lineaires de V avec la ligne de A
            }
            res[i] = trueMod(sum, 26); // on ramène les valeurs à l'intervalle [0, 26]
        }
        return res;
    }

    // Fonction effectuant la multiplication d'une matrice A par une constante C
    private int[][] multMatrixConst(int[][] A, int C){
        int sizeA = A.length;
        for(int i=0;i<sizeA;i++){
            for(int j=0;j<sizeA;j++){ // on multiplie chaque valeur de la matrice A par la constante C
                A[i][j] = (A[i][j] * C)%26; // le modulo ici n'est pas obligatoire mais simplifie la matrice et donc les combinaisons linéaires pour décrypter
            }
        }
        return A;
    }
}
