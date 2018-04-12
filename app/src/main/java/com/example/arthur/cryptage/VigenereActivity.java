package com.example.arthur.cryptage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class VigenereActivity extends AppCompatActivity {

	private EditText mEtInput; // message à coder ou à décoder
	private EditText mEtKey; // mot clé permettant de décaler les lettres du message
	private TextView mTvRes; // resultat du cryptage ou du décryptage
	private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
	private Switch mSwEtendu; // alphabet standard (97-122) ou étendu (32-126)

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_cesar );
		initView();
	}
	private void initView() {
		mEtInput = findViewById( R.id.etInput );
		mEtKey = findViewById( R.id.etKey);
		mEtKey.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME); // dans le XML "activity_cesar" la clé est un nombre tandis qu'ici on veut un mot
		mTvRes = findViewById( R.id.tvRes );
		mBtRun = findViewById( R.id.btRun );
        mSwEtendu = findViewById( R.id.swEtendu );
		Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec); // option permettant de selectionner le codage ou le decodage du message
		mSpinnerCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
				String selectedItem = parent.getItemAtPosition(position).toString();
				if(selectedItem.equals("Coder")){
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
            if(input.isEmpty()){ // si le message est vide on affiche une erreur
                Toast.makeText(this, "Veuillez saisir un message à coder ou décoder", Toast.LENGTH_LONG).show();
                return;
            }
            String codec = mBtRun.getText().toString();
            String key = mEtKey.getText().toString();
            if(key.isEmpty()){ // si la clé est vide on affiche une erreur
                Toast.makeText(this, "Veuillez saisir une clé", Toast.LENGTH_LONG).show();
                return;
            }
            String res = null;
            try {
                int crypt = codec.equals( "Coder" ) ? 1 : -1;
                res = algoVigenere(input, key, crypt ); // on décode ou on code le message avec le mot clé spécifié
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
            mTvRes.setText( res );
        });
	}

    /*
       Fonction de cryptage ou de décryptage utilisant l'algorithme de Vigenere
       En entrée: String input = message à coder / décoder
                  String key = le mot clé pour décaler les lettres du message
                  int crypt = valeur entière égale à 1 si on code et -1 si on décode
       En sortie: Une chaine de caractère représentant le codage ou le décodage du message
    */
	private String algoVigenere( String input, String key, int crypt) throws Exception{
        int modulo, start_alphabet;
        if (mSwEtendu.isChecked()) { // si l'option d'alphabet etendu est activée
            modulo = 95; // le modulo sera de 127-32 = 95
            start_alphabet = 32; // l'alphabet commencera au caractère 32 en ASCII = ' '
        } else{
            modulo = 26; // l'alphabet standard contient seulement 26 lettres
            start_alphabet = 97; // l'alphabet commencera au caractère 97 en ASCII = 'a'
        }
		int inputSize = input.length(); // taille du message
		StringBuilder concatKey = new StringBuilder(key);
		while(concatKey.length()<inputSize){ // on concatene la clé avec elle meme jusqu'à ce que sa taille soit >= à celle du message
			concatKey.append(key);
		}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<inputSize;i++){ // pour chaque lettre dans le message
			char ci = input.charAt( i ); // lettre du message
			char ck = concatKey.charAt( i ); // lettre correspondante dans la clé

			if( isOutOfRange(ci, start_alphabet, modulo)) // si l'un des deux caractères n'appartient pas à l'alphabet selectionné on renvoie une erreur
                throw new Exception("Le caractère "+ci+" du message n'est pas dans la plage de caractères de l'alphabet selectionné");
            if( isOutOfRange(ck, start_alphabet, modulo))
                throw new Exception("Le caractère "+ck+" de la clé n'est pas dans la plage de caractères de l'alphabet selectionné");

            // on ramène les caractères 'ci' et 'ck' à l'intervalle [0, modulo] puis on décale ci avec ck avec en restant dans cet intervalle
			int decal = ( decalAscii(ci, start_alphabet) + crypt * decalAscii(ck, start_alphabet)) % modulo;
			decal = decal >= 0 ? decal : decal + modulo; // le modulo en java peut être négatif
			ci = (char) ( decal + start_alphabet); // on ramène le resultat à l'intervalle [start_alphabet, star_alphabet+modulo]
			sb.append(ci); // on ajoute le caractère codé / décodé dans la chaîne de caractères du résultat
		}
		return sb.toString();
	}

	// Fonction décalant une lettre c dans l'intervalle [0, modulo] de la table ASCII
    // inutile mais clarifie un peu le code
	private int decalAscii( char c, int start_alphabet){
		return ((int) c - start_alphabet );
	}
	// Fonction determinant si une lettre est en dehors de la plage autorisée [start_alphabet, start_alphabet+modulo[ de la table ASCII
	private boolean isOutOfRange(char c, int start_alphabet, int modulo){
		return (int)c < start_alphabet || (int)c >= (start_alphabet+modulo);
	}
}
