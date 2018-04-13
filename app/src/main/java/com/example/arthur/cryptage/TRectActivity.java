package com.example.arthur.cryptage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Comparator;

/********************************************************************
 * Classe implémentant un algorithme de transposition rectangulaire *
 ********************************************************************/
public class TRectActivity extends AppCompatActivity {

	private EditText mEtInput; // message à coder ou à décoder
	private EditText mEtKey; // clé permettant de construire le tableau de transposition
	private TextView mTvRes; // resultat du cryptage ou du décryptage
	private Button mBtRun; // bouton executant l'algorithme de cryptage / décryptage
    boolean crypt = true; // valeur booleene égale à vrai si on code et faux si on decode

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_trect );
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
			public void onNothingSelected( AdapterView<?> adapterView ) {}
		} );

		mBtRun.setOnClickListener(view -> {
            String input = StringOperations.getOnlyLetters(mEtInput.getText().toString()); // on recupère uniquement les lettres dans le message
			if(input.isEmpty()){ // si le message est vide on affiche une erreur
				Toast.makeText(this, "Le message à coder ou à décoder doit être un mot ou une phrase", Toast.LENGTH_LONG).show();
				return;
			}
            String codec = mBtRun.getText().toString();
            String key = StringOperations.getOnlyLetters(mEtKey.getText().toString()); // on recupère uniquement les lettres dans la clé
			if(key.isEmpty()){ // si la clé est vide on affiche une erreur
				Toast.makeText(this, "La clé doit être un mot ou une phrase", Toast.LENGTH_LONG).show();
				return;
			}
			String res = crypt ? codeTRect(input, key) : decodeTRect(input, key); // on code ou on décode le message en focntion de l'option choisie
            mTvRes.setText( res );
        });
	}

	// Fonction récupérant l'ordre des colonnes à remplir pour initialiser le tableau de transposition
    // Exemple: avec le mot clé HELLO on obtiendra l'ordre: 1 0 2 3 4 car E > H > L > O
	private Integer[] getColumnOrder( String key){
		char[] arr = key.toCharArray();
		ArrayIndexComparator comparator = new ArrayIndexComparator(arr);
		Integer[] indexes = comparator.createIndexArray();
		Arrays.sort(indexes, comparator);
		return indexes;
	}

	// On pourrait faire beaucoup plus concis avec les expressions lambda de Java 8
	// Malheureusement ce n'est pas compatible avec toutes les versions de Java et toutes les plateformes d'Android...
	/* @RequiresApi(api = Build.VERSION_CODES.N)
	private List<Integer> getColumnOrder( String key){
		char[] arr = key.toCharArray();
		List<Integer> range = IntStream.rangeClosed( 0, arr.length - 1 ).boxed().collect( Collectors.toList() );
		range.sort( Comparator.comparingInt( c -> arr[c]));
		return range;
	} */

	// Fonction codant un message avec une transposition rectangulaire
	private String codeTRect( String input, String key ) {
		Integer[] colOrder = getColumnOrder( key ); // on determine l'ordre de remplissage du tableau de transposition avec la clé saisie par l'utilisateur
		int key_length = key.length(); // longueur de la clé
		int input_length = input.length(); // longueur du message
		int nb_rows = input_length % key_length == 0 ? input_length/key_length: input_length/key_length +1; // nombre de lignes du tableau de transposition  = Math.ceil(longueur message / longueur clé)
		char[][] tMatrix = new char[nb_rows][key_length]; // tableau de transposition
		char[] inputArr = input.toCharArray(); // on converti le message en tableau de caractères

		for(int i=0;i<input_length;i++){ // pour chaque lettre dans le message
			int row = i/key_length;
			int col = i%key_length;
			tMatrix[row][col] = inputArr[i]; // on ajoute la lettre aux coordonnées appropriées du tableau de transposition
		}
		int specialColumns = input_length%key_length == 0 ? key_length : input_length%key_length; // nombre de colonnes ayant un element de plus que les autres
		StringBuilder sb = new StringBuilder();

		for(Integer nextCol : colOrder){ // on parcourt les colonnes dans l'ordre spécifié par la clé
			int boundary = nextCol<specialColumns ? nb_rows : nb_rows - 1;  // on calcule le nombre d'élement contenus dans cette colonne
			for(int j=0;j<boundary;j++){ // on concatène les lettres de cette colonne avec le résultat
				sb.append(tMatrix[j][nextCol]);
			}
		}
		return sb.toString();
	}

    // Fonction décodant un message avec une transposition rectangulaire
	private String decodeTRect( String input, String key ) {
		Integer[] colOrder = getColumnOrder( key ); // on determine l'ordre de remplissage du tableau de transposition avec la clé saisie par l'utilisateur
		int key_length = key.length(); // longueur de la clé
		int input_length = input.length(); // longueur du message
		int nb_rows = input_length%key_length == 0 ? input_length/key_length: input_length/key_length +1; // nombre de lignes du tableau de transposition  = Math.ceil(longueur message / longueur clé)
		char[][] tMatrix = new char[nb_rows][key_length]; // tableau de transposition
		char[] inputArr = input.toCharArray(); // on converti le message en tableau de caractères
		int specialColumns = input_length%key_length == 0 ? key_length : input_length%key_length; // nombre de colonnes ayant un element de plus que les autres
		int cptInput = 0;
        // on reconstruit la matrice de transposition à partir de la clé et du codage du message
		for(Integer nextCol : colOrder){ // on parcourt les colonnes dans l'ordre spécifié par la clé
			int boundary = nextCol<specialColumns ? nb_rows : nb_rows - 1; // on calcule le nombre d'élement contenus dans cette colonne
			for(int j=0;j<boundary;j++){
				tMatrix[j][nextCol] = inputArr[cptInput++]; // on ajoute le lettre du codage à l'emplacement approprié dans le tableau de transposition
			}
		}
		StringBuilder sb = new StringBuilder(); // on lit le résultat ligne par ligne dans la matrice de transposition obtenue
		for(int i=0;i<nb_rows-1;i++){ // pour toutes les lignes ayant le même nombre d'élements
			for(int j=0;j<key_length;j++){
				sb.append(tMatrix[i][j]); // on concatène les lettres ligne par ligne
			}
		}
		for(int j=0;j<specialColumns;j++){ // la derniere ligne contient "specialColumns" elements
			sb.append(tMatrix[nb_rows-1][j]); // on ajoute les lettres de cette ligne au résultat
		}
		return sb.toString();
	}

	// Classe interne permettant de comparer des caractères entre eux
    // cette classe est utilisée pour recupérer directement l'ordre des colonnes induit par la clé
	private class ArrayIndexComparator implements Comparator<Integer>{
		private final char[] array;
		ArrayIndexComparator(char[] array){
			this.array = array;
		}
		Integer[] createIndexArray(){
			Integer[] indexes = new Integer[array.length];
			for (int i = 0; i < array.length; i++){
				indexes[i] = i;
			}
			return indexes;
		}
		@Override
		public int compare(Integer index1, Integer index2){
			// conversion implicite de Integer vers int  pour utiliser les indices des tableaux
			return Character.compare(array[index1], array[index2]);
		}
	}
}