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

public class PolybeActivity extends AppCompatActivity {

	private EditText mEtInput; // message à coder ou à décoder
	private EditText mEtKey; // clé permettant de construire le carré de Polybe
    private TextView mTvRes; // resultat du cryptage ou du décryptage
	private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
	private EditText mEtLetter; // lettre à remplacer dans le carré
	private Polybe polybe; // classe modélisant un carré de Polybe
	boolean crypt = true; // valeur booleene égale à vrai si on code et faux si on decode

	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_polybe );
		initView();
	}

	private void initView() {
		mEtInput = findViewById( R.id.etInput );
		mEtLetter = findViewById( R.id.etLetter );
		mEtKey = findViewById( R.id.etKey);
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
            String input = mEtInput.getText().toString();
            String codec = mBtRun.getText().toString();
            // le texte a déjà été analysé par la classe Polybe après la saisie
            // si aucune clé n'est entrée alors le carré contient les lettre de de l'alphabet dans l'ordre (sans la lettre de remplacement)
            String key = mEtKey.getText().toString();
            String strReplace = StringOperations.getOnlyLetters(mEtLetter.getText().toString());
            if(strReplace.isEmpty()){ // si le champs contenant la lettre à remplacer est vide on affiche une erreur (champs vide ou champs contenant un caractère spécial)
                Toast.makeText(this, "La lettre à remplacer dans le carré de polybe est incorrecte", Toast.LENGTH_LONG).show();
                return;
            }
            char replace = strReplace.charAt(0);
            polybe = new Polybe(key, replace); // on initialise un carré de Polybe avec le mot clé et la lettre de remplacement
            String res;

            if (!crypt){ // si l'option de décodage est selectionnée
                input = input.replaceAll("[^1-5]+", ""); // on remplace les caractères du message qui ne sont pas des chiffres compris entre 1 et 5
                if(input.length()%2==1) { // si la taille du message n'est pas un multiple de 2 on affiche une erreur
					Toast.makeText(this, "Décodage avec un message de longueur impaire.\nVérifiez le message codé.", Toast.LENGTH_LONG).show();
					return;
                }
                res = decodePolybe(input); // sinon on décode le message
            } else { // si on code le message
                input = StringOperations.getOnlyLetters(input); // on recupère uniquement les lettres dans le message
                if(input.isEmpty()){ // si le message est vide on affiche une erreur
                    Toast.makeText(this, "Le message à coder ou à décoder doit être un mot ou une phrase", Toast.LENGTH_LONG).show();
                    return;
                }
                res = codePolybe(input); // sinon on code le message
            }

            mTvRes.setText( res );
        });
	}

	// Fonction codant un message avec un carré de Polybe
	private String codePolybe(String input) {
		StringBuilder sb = new StringBuilder();
		for(char c: input.toCharArray()){ // pour chaque lettre dans le message
			int[] coords = polybe.getCoords(c); // on récupère les coordonnées de la lettre dans le carré
			String strCoords = String.valueOf( coords[0]+1 )+String.valueOf( coords[1]+1 );
			sb.append( strCoords ); // on concatène ces coordonnées dans le résultat
		}
		return sb.toString();
	}

    // Fonction décodant un message avec un carré de Polybe
	private String decodePolybe(String input) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<=input.length()-2; i+=2){ // pour chaque couple de chiffre dans le message
			int row = Character.getNumericValue(input.charAt(i)) -1; // le premier chiffre correspond au numéro de ligne de la lettre décodée
			int col = Character.getNumericValue(input.charAt(i+1)) -1; // le second chiffre correspond au numéro de colonne
			sb.append(polybe.getLetter(row,col)); // on récupère la lettre correspondant à ces coordonnées dans le carré de Polybe et ou l'ajoute au résultat
		}
		return sb.toString();
	}
}
