package com.example.arthur.cryptage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class AtbashActivity extends AppCompatActivity {

	private EditText mEtInput; // message à coder ou à décoder
	private TextView mTvRes; // resultat du cryptage ou du décryptage
    private Switch mSwEtendu; // alphabet standard (97-122) ou étendu (32-126)

	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_atbash );
		initView();
	}

	private void initView() {
		mEtInput = findViewById( R.id.etInput );
		mTvRes = findViewById( R.id.tvRes );
        Button mBtRun = findViewById(R.id.btRun); // bouton executant l'algorithme de cryptage / décryptage
		mSwEtendu = findViewById( R.id.swEtendu );
		mBtRun.setOnClickListener(view -> {
            String input = mEtInput.getText().toString();
            if(input.isEmpty()){ // si le message est vide on affiche une erreur
                Toast.makeText(this, R.string.input_missing, Toast.LENGTH_LONG).show();
                return;
            }
            try {
                mTvRes.setText( algoAtbash(input) ); // on execute l'algorithme sur le message
            } catch (Exception e) { // si un caractère du message n'est pas dans la plage de caractères de l'alphabet standard ou étendu on affiche une erreur
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
	}

	private String algoAtbash( String input) throws Exception{ // l'algorithme de cryptage est identique à l'algorithme de décryptage
		/* La plage de caractères autorisée dépend de l'alphabet selectionné
           pour l'alphabet étendu les lettres appartiennent à l'intervalle [32, 126] de la table ASCII
           pour l'alphabet standard les lettres appartiennent à l'intervalle [97, 122] de la table ASCII */
		int end_alphabet, start_alphabet;
		if (mSwEtendu.isChecked()) { // si l'option d'alphabet etendu est activée
			start_alphabet = 32; // l'alphabet commencera au caractère 32 en ASCII = ' '
            end_alphabet = 126; // l'alphabet finira au caractère 126 en ASCII = '~'
		} else{
			start_alphabet = 97; // l'alphabet commencera au caractère 97 en ASCII = 'a'
            end_alphabet = 122;  // l'alphabet finira au caractère 122 en ASCII = 'z'
		}

		StringBuilder sb = new StringBuilder();
		for(char c: input.toCharArray()){ // pour chaque caractère dans le message
			if((int)c < start_alphabet || (int)c >= (start_alphabet + end_alphabet)) // si ce caractère n'appartient pas à l'alphabet selectionné on renvoie une erreur
				throw new Exception("Le caractère "+c+" n'est pas dans la plage de caractères de l'alphabet selectionné");
            c = (char) (end_alphabet - (int) c + start_alphabet) ; // on détermine la position dans la table ASCII du caractère mirroir de 'c' sur l'alphabet selectionné
			sb.append(c); // on ajoute le miroir de 'c' dans la chaîne de caractère du résultat
		}
		return sb.toString();
	}
}
