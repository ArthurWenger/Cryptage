package com.example.arthur.cryptage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class CesarActivity extends AppCompatActivity{

	private EditText mEtInput; // message à coder ou à décoder
	private EditText mEtDecalage; // clé correspondant à la valeur du décalage de chaque lettre dans l'alphabet
	private TextView mTvRes; // resultat du cryptage ou du décryptage
	private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
	private Switch mSwEtendu; // alphabet standard (97-122) ou étendu (32-126)
    int crypt = 1; // valeur booleene égale à vrai si on code et faux si on decode

	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_cesar );
		initView();
	}

	private void initView() {
		mEtInput = findViewById( R.id.etInput );
		mEtDecalage = findViewById( R.id.etKey);
		mTvRes = findViewById( R.id.tvRes );
		mBtRun = findViewById( R.id.btRun );
		mSwEtendu = findViewById( R.id.swEtendu );
		Spinner mSpinnerCodec = findViewById(R.id.spinnerCodec); // option permettant de selectionner le codage ou le decodage du message
		mSpinnerCodec.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                String selectedItem = parent.getItemAtPosition( position ).toString();
                String code = getString(R.string.code );
                String decode = getString(R.string.decode );
                if ( selectedItem.equals( code )) {
                    mBtRun.setText( code );
                    crypt = 1;
                } else {
                    mBtRun.setText(decode);
                    crypt = -1;
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
            String decal = mEtDecalage.getText().toString();
            if(decal.isEmpty()){ // si le champs de décalage est vide on affiche une erreur
                Toast.makeText(this, "Veuillez saisir la valeur du décalage", Toast.LENGTH_LONG).show();
                return;
            }
            int d = Integer.valueOf( decal );
            try {
                mTvRes.setText(algoCesar(input, d, crypt )); // on décode ou on code le message avec le décalage specifié
            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
	}

	/*
	   Fonction de cryptage ou de décryptage utilisant l'algorithme de César
	   En entrée: String input = message à coder / décoder
	   	          int decal = valeur du décalage dans l'alphabet
	              int crypt = valeur entière égale à 1 si on code et -1 si on décode
	   En sortie: Une chaine de caractère représentant le codage ou le décodage du message
	*/
	private String algoCesar( String input, int decal, int crypt) throws Exception{ // l'algorithme de cryptage est identique à l'algorithme de décryptage
		/* La plage de caractères autorisée dépend de l'alphabet selectionné
           pour l'alphabet étendu les lettres appartiennent à l'intervalle [32, 126] de la table ASCII
           pour l'alphabet standard les lettres appartiennent à l'intervalle [97, 122] de la table ASCII */
		int modulo, start_alphabet;
		if (mSwEtendu.isChecked()) { // si l'option d'alphabet etendu est activée
			modulo = 95; // le modulo sera de 127-32 = 95
			start_alphabet = 32; // l'alphabet commencera au caractère 32 en ASCII = ' '
		} else{
			modulo = 26; // l'alphabet standard contient seulement 26 lettres
			start_alphabet = 97; // l'alphabet commencera au caractère 97 en ASCII = 'a'
		}

		StringBuilder sb = new StringBuilder();
		for(char c: input.toCharArray()){ // pour chaque caractère dans le message
		    if((int)c < start_alphabet || (int)c >= (start_alphabet + modulo)) // si ce caractère n'appartient pas à l'alphabet selectionné on renvoie une erreur
		        throw new Exception("Le caractère "+c+" n'est pas dans la plage de caractères de l'alphabet selectionné");
            // on ramène le caractère 'c' à l'intervalle [0, modulo] puis on le décale en restant dans cet intervalle
			int mod = (((int) c - start_alphabet ) + crypt * decal) % modulo; // si on code la lettre alors on ajoute le decalage sinon ou soustrait le décalage
			// le modulo peut prendre une valeur négative en Java (exemple: -7 % 5 = -2)
			mod = mod >= 0 ? mod : mod + modulo; // pour pallier à ce problème, on ajoute une fois le modulo s'il est négatif (exemple: -2 % 5 = (-2+5) % 5 = 3)
			c = (char) ( mod + start_alphabet); // on ramène le resultat à l'intervalle [start_alphabet, star_alphabet+modulo]
			sb.append(c); // on ajoute le caractère codé / décodé dans la chaîne de caractères du résultat
		}
		return sb.toString();
	}
}
