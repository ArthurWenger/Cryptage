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

public class PlayfairActivity extends AppCompatActivity {

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
		mEtKey.setOnEditorActionListener((v, actionId, event) -> {  // la clé saisie par l'utilisateur est formattée pour correspondre au début du carré de Polybe qui sera construit
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) { // lorsque l'utilisateur valide la saisie
                String key = mEtKey.getText().toString(); // on recupère la chaîne de caractères correspondant à la clé
                mEtKey.setText( Polybe.parseKey(key) ); // on réecrit la clé pour que chaque lettre n'apparaisse qu'une seule fois
            }
            return false;
        });

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

		mBtRun.setOnClickListener(view -> {
            String input = StringOperations.getOnlyLetters(mEtInput.getText().toString());
            if(input.isEmpty()){ // si le message est vide on affiche une erreur
                Toast.makeText(this, "Le message à coder ou à décoder doit être un mot ou une phrase", Toast.LENGTH_LONG).show();
                return;
            }
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
                if(input.length()%2==1) { // si la taille du message n'est pas un multiple de 2 on affiche une erreur
					Toast.makeText(this, "Décodage avec un message de longueur impaire.\nVérifiez le message codé.", Toast.LENGTH_LONG).show();
                    return;
                }
                res = decodePlayfair(input); // sinon on décode le message
            } else { // sinon on code le message
                res = codePlayfair(input);
            }
            mTvRes.setText( res );
        });
	}

	// Fonction décodant un message avec Playfair
	private String decodePlayfair(String input) {
		StringBuilder sb = new StringBuilder();
		char[] inputArr = input.toCharArray(); // on converti le message en tableau de caractères
		// pour chaque couple de lettres dans le message
		for(int i=0;i<inputArr.length-1;i+=2) { // on peut s'arrêter à inputArr.length-1 car la longueur du message est paire
			char c1 = inputArr[ i ]; // on recupère la première lettre du couple
			char c2 = inputArr[ i + 1 ]; // on recupère la deuxième lettre du couple
			String decrypt_c1c2 = codecTwoChar(c1, c2, -1);  // on décode le couple de lettre
			sb.append(decrypt_c1c2); // on ajoute le decodage au resultat
		}
		return sb.toString();
	}

	// Fonction codant un message avec Playfair
	private String codePlayfair(String input) {
		StringBuilder sb = new StringBuilder();
		char[] inputArr = input.toCharArray(); // on converti le message en tableau de caractères

		for(int i=0;i<inputArr.length;i+=2){ // pour chaque couple de lettres dans le message
			char c1 = inputArr[i]; // on recupère la première lettre du couple
			char c2; // variable représentant la deuxième lettre du couple
			// si la longueur du message est impaire alors le dernier caractère sera associé à la lettre 'x'
			if(i != inputArr.length-1){
				c2 = inputArr[ i + 1 ];
			} else { // si on atteint l'avant dernier indice du message alors il ne reste qu'une seule lettre à coder. Dans ce cas on rajoute un 'x' pour completer le message
				c2 = 'x';
			}
			// si les deux lettres sont identiques alors on code le couple (c1,'x') ou (c1,'q') si c1='x'
			// c2 sera traité à la prochaine itération de la boucle
			if(c1 == c2){
				if(c1 != 'x')
					c2 = 'x';
				else // cas rare de la séquence "XX"
					c2 = 'q';
				// i-1+2 = i+1 donc le caractère traité à la prochaine itération sera celui d'indice i+1=c2
				i--;
			}
			String crypt_ch1ch2 = codecTwoChar(c1, c2, 1); // on code le couple de lettres
			sb.append(crypt_ch1ch2); // on ajoute le codage au résultat
		}
		return sb.toString();
	}

	// Cryptage ou decryptage de deux caractères en fonction de leur position dans le carré de polybe
	// si decal = 1 on code les caractères, si decal = -1 on les décode
	private String codecTwoChar(char c1, char c2, int decal){
		// lignes et colonnes de c1 et c2 dans le carré de polybe
		int[] rc1 = polybe.getCoords(c1);
		int[] rc2 = polybe.getCoords(c2);
		// matrice représentant le carré de Polybe
		char[][] mat = polybe.matrixPolybe();

		char res_c1, res_c2;
		// les 2 caractères sont sur la même ligne
		if(rc1[0] == rc2[0]){
			// on décale c1 et c2 vers la droite ou vers la gauche
			res_c1 = mat[ rc1[0] ][ trueMod(rc1[1]+decal,5) ];
			res_c2 = mat[ rc2[0] ][ trueMod(rc2[1]+decal,5) ];
		}
		// les 2 caractères sont sur la même colonne
		else if (rc1[1] == rc2[1]){
			// on décale c1 et c2 vers le bas ou vers le haut
			res_c1 = mat[ trueMod(rc1[0]+decal,5) ][ rc1[1] ];
			res_c2 = mat[ trueMod(rc2[0]+decal,5) ][ rc2[1] ];
		}
		// les 2 caractères ne sont ni sur la meme ligne ni sur la même colonne
		else {
			// on prend les coins opposés du rectangle formé par c1 et c2
			res_c1 = mat[ rc1[0] ][ rc2[1] ];
			res_c2 = mat[ rc2[0] ][ rc1[1] ];
		}
		return ""+res_c1+res_c2;
	}

	// le modulo '%' en java peut être négatif
	private int trueMod(int n, int mod){
		int res = n%mod;
		return res >= 0 ? res : res + mod;
	}
}
