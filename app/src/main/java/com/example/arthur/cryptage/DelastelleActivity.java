package com.example.arthur.cryptage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class DelastelleActivity extends AppCompatActivity {

	private EditText mEtInput; // message à coder ou à décoder
	private EditText mEtKey; // clé permettant de construire le carré de Polybe
    private EditText mEtBlocSize; // taille des blocs de lettres
    private EditText mEtLetter; // lettre à remplacer dans le carré
    private TextView mTvRes; // resultat du cryptage ou du décryptage
	private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
    private Polybe polybe; // classe modélisant un carré de Polybe
    boolean crypt = true; // valeur booleene égale à vrai si on code et faux si on decode

    @Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_delastelle );
        initView();
	}
	private void initView() {
		mEtInput = findViewById( R.id.etInput );
		mEtKey = findViewById( R.id.etKey);
        mEtBlocSize = findViewById( R.id.etBlocSize );
        mEtLetter = findViewById( R.id.etLetter );
		mTvRes = findViewById( R.id.tvRes );
		mBtRun = findViewById( R.id.btRun );
        Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec); // option permettant de selectionner le codage ou le decodage du message
		mSpinnerCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
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
			public void onNothingSelected( AdapterView<?> adapterView ) {
			}
		} );

        mEtKey.setOnEditorActionListener((v, actionId, event) -> { // la clé saisie par l'utilisateur est formattée pour correspondre au début du carré de Polybe qui sera construit
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) { // lorsque l'utilisateur valide la saisie
                String key = mEtKey.getText().toString(); // on recupère la chaîne de caractères correspondant à la clé
                mEtKey.setText( Polybe.parseKey(key) ); // on réecrit la clé pour que chaque lettre n'apparaisse qu'une seule fois
            }
            return false;
        });

		mBtRun.setOnClickListener(view -> {
            String input = StringOperations.getOnlyLetters(mEtInput.getText().toString());
            if(input.isEmpty()){ // si le message est vide on affiche une erreur
                Toast.makeText(this, R.string.input_missing_word, Toast.LENGTH_LONG).show();
                return;
            }
            String codec = mBtRun.getText().toString();
            // le texte a déjà été analysé par la classe Polybe après la saisie
            // si aucune clé n'est entrée alors le carré contiendra les lettres de de l'alphabet dans l'ordre (sans la lettre de remplacement)
            String key = mEtKey.getText().toString();
            String strReplace = StringOperations.getOnlyLetters(mEtLetter.getText().toString());
            if(strReplace.isEmpty()){ // si le champs contenant la lettre à remplacer est vide on affiche une erreur (champs vide ou champs contenant un caractère spécial)
                Toast.makeText(this, R.string.letter_missing, Toast.LENGTH_LONG).show();
                return;
            }
            char replace = strReplace.charAt(0);
            String strBlocSize = mEtBlocSize.getText().toString();
            if(strBlocSize.isEmpty()){ // si la taille des blocs est vide on affiche une erreur
                Toast.makeText(this, R.string.block_size_missing, Toast.LENGTH_LONG).show();
                return;
            }
            int blocSize = Integer.valueOf(mEtBlocSize.getText().toString()); // la taille des blocs de lettres est nécessairement un entier en raison du champs android:inputtype="number" de l'EditText
            if(blocSize == 0){ // si la taille des blocs est égale à 0 on affiche une erreur
                Toast.makeText(this, R.string.block_size_0, Toast.LENGTH_LONG).show();
                return;
            }
            polybe = new Polybe(key, replace); // on initialise un carré de Polybe avec le mot clé et la lettre de remplacement
            String res = crypt ? codeDelastelle(input, blocSize) : decodeDelastelle(input, blocSize); // on decode ou on code le message en fonctiond e l'option selectionnée
            mTvRes.setText( res ); // on affiche le résultat du codage ou du décodage
        });
	}

    /*
       Fonction de codage utilisant l'algorithme de Delastelle
       En entrée: String input = message à coder
                  int blocSize = taille des blocs de lettres utilisée pour scinder le message en plusieurs morceaux
       En sortie: Une chaine de caractère représentant le codage du message
    */
    private String codeDelastelle(String input, int blocSize) {
        // on découpe le message en groupes de lettre de longueur "blocSize"
        // si la taille du message n'est pas un multiple de "blocSize" au complète le message avec la lettre 'x'
        String[] input_blocs =  StringOperations.splitStringEqual(input, blocSize, 'x');
        StringBuilder res = new StringBuilder(input.length()); // chaine de caractères représentant le codage final

        for(String bloc: input_blocs){ // pour chaque bloc de lettre
            ArrayList<Integer> rowConcat = new ArrayList<>(blocSize); // liste représentant les numéros de ligne successifs de chaque lettre dans le carré de Polybe
            ArrayList<Integer> colConcat = new ArrayList<>(blocSize); // liste représentant les numéros de colonne successifs de chaque lettre dans le carré de Polybe
            for(char c : bloc.toCharArray()){ // pour chaque lettre du bloc
                int[] coords = polybe.getCoords(c); // on détermine les coordonnées de la lettre c dans le carré de polybe
                rowConcat.add(coords[0]); // on ajoute le numéro de ligne dans la liste rowConcat
                colConcat.add(coords[1]); // on ajoute le numéro de colonne dans la liste colConcat
            }
            rowConcat.addAll(colConcat); // on concatène les numéros de ligne et les numeros de colonne de toutes les lettres du bloc

            for(int i=0;i<rowConcat.size()-1;i+=2){ // pour chaque chaque couple de numeros de la liste (la longueur de la liste est necessairement paire)
                int codeRow = rowConcat.get(i); // le numéro de ligne de la lettre codée correspondra au premier numero du couple
                int codeCol = rowConcat.get(i+1);  // le numéro de colonne de la lettre codée correspondra au second numero du couple
                res.append(polybe.getLetter(codeRow, codeCol)); // on transforme le couple de coordonnées en lettre avec le carré de Polybe puis on ajoute cette lettre au resultat
            }
        }
        return res.toString(); // on retourne le résultat du codage
    }

    /*
       Fonction de decodage utilisant l'algorithme de Delastelle
       En entrée: String input = message à décoder
                  int blocSize = taille des blocs de lettres utilisée pour scinder le message en plusieurs morceaux
       En sortie: Une chaine de caractère représentant le décodage du message
    */
    private String decodeDelastelle(String input, int blocSize) {
        // on découpe le message en groupes de lettre de longueur "blocSize"
        // on considère qu'il soit possible que la longueur du message à décoder ne soit pas multiple de blocSize dans le cas d'un oubli de lettres par exemple
        String[] input_blocs =  StringOperations.splitStringEqual(input, blocSize, 'x');
        StringBuilder res = new StringBuilder(input.length()); // chaine de caractère représentant le décodage final
        int[] blocConcat = new int[2*blocSize]; // tableau qui contiendra la concaténation des coordonnées de chaque lettre d'un bloc
        int[] blocRow; // tableau qui contiendra les numéros de ligne des lettres du message original
        int[] blocCol; // tableau qui contiendra les numéros de colonne des lettres du message original

        // on pourrait faire plus efficace en analysant la parité de la taille de blocs mais le code serait moins lisible pour un gain minime
        for(String bloc: input_blocs){ // pour chaque bloc de lettre
            for(int i=0;i<blocSize;i++){ // pour chaque lettre dans le bloc
                char c = bloc.charAt(i);
                int[] coords = polybe.getCoords(c); // on détermine les coordonnées de la lettre c dans le carré de polybe
                int start = 2*i; // les coordonnées seront ajoutées au tableau blocConcat à la position 2*i
                blocConcat[start] = coords[0]; // le numéro de ligne de la lettre est ajouté à la suite du tableau blocConcat
                blocConcat[start+1] = coords[1]; // le numéro de colonne de la lettre est ajouté après le numéro de ligne
            }
            blocRow = Arrays.copyOf(blocConcat, blocSize); // les numéros de ligne des lettres du message original sont sur la première moitié du tableau blocConcat
            blocCol = Arrays.copyOfRange(blocConcat, blocSize, 2*blocSize); // les numéros de ligne des lettres du message original sont sur la seconde moitié du tableau blocConcat

            for(int i=0;i<blocSize;i++){ // on reconstruit les lettres du message original pour ce bloc
                char c = polybe.getLetter(blocRow[i],blocCol[i]); // pour retrouver chaque lettre du message original on utilise les listes blocRow et blocCol comme coordonnées dans le carré de polybe
                res.append(c); // on ajoute chaque lettre décodée au résultat final
            }
        }
        return res.toString();
    }
}
