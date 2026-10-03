package com.example.quizfootball

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


data class Question(
    val texte: String,
    val image: Int,
    val choix: List<String>,
    val bonneReponse: Int,
    val description: String
)

class MainActivity : ComponentActivity() {

    private lateinit var txtScore: TextView
    private lateinit var progressQuiz: ProgressBar
    private lateinit var layoutQuestion: LinearLayout
    private lateinit var txtNumeroQuestion: TextView
    private lateinit var imgQuestion: ImageView
    private lateinit var txtQuestion: TextView
    private lateinit var btnChoix1: Button
    private lateinit var btnChoix2: Button
    private lateinit var btnChoix3: Button
    private lateinit var txtFeedback: TextView
    private lateinit var btnSuivant: Button
    private lateinit var layoutResultat: LinearLayout
    private lateinit var txtResultatScore: TextView
    private lateinit var btnRejouer: Button

    private lateinit var boutonsChoix: List<Button>

    private lateinit var questions: List<Question>

    private var indexQuestionCourante = 0
    private var score = 0
    private var aDejaRepondu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initialiserQuestions()
        recupererComposants()
        configurerClics()

        if (savedInstanceState != null) {
            indexQuestionCourante = savedInstanceState.getInt(CLE_INDEX, 0)
            score = savedInstanceState.getInt(CLE_SCORE, 0)
        }

        afficherQuestionCourante()
    }

    private fun initialiserQuestions() {
        questions = listOf(
            Question(
                texte = getString(R.string.q1_texte),
                image = R.drawable.joueur1,
                choix = listOf(
                    getString(R.string.q1_choix1),
                    getString(R.string.q1_choix2),
                    getString(R.string.q1_choix3)
                ),
                bonneReponse = 0,
                description = getString(R.string.desc_joueur1)
            ),
            // Q2 : joueur2.jpg = Neymar -> bonne réponse = choix2 (indice 1)
            Question(
                texte = getString(R.string.q2_texte),
                image = R.drawable.joueur2,
                choix = listOf(
                    getString(R.string.q2_choix1),
                    getString(R.string.q2_choix2),
                    getString(R.string.q2_choix3)
                ),
                bonneReponse = 1,
                description = getString(R.string.desc_joueur2)
            ),
            Question(
                texte = getString(R.string.q3_texte),
                image = R.drawable.equipe1,
                choix = listOf(
                    getString(R.string.q3_choix1),
                    getString(R.string.q3_choix2),
                    getString(R.string.q3_choix3)
                ),
                bonneReponse = 1,
                description = getString(R.string.desc_equipe1)
            ),
            Question(
                texte = getString(R.string.q4_texte),
                image = R.drawable.equipe2,
                choix = listOf(
                    getString(R.string.q4_choix1),
                    getString(R.string.q4_choix2),
                    getString(R.string.q4_choix3)
                ),
                bonneReponse = 2,
                description = getString(R.string.desc_equipe2)
            ),
            Question(
                texte = getString(R.string.q5_texte),
                image = R.drawable.coupe,
                choix = listOf(
                    getString(R.string.q5_choix1),
                    getString(R.string.q5_choix2),
                    getString(R.string.q5_choix3)
                ),
                bonneReponse = 1,
                description = getString(R.string.desc_trophee)
            )
        )
    }

    private fun recupererComposants() {
        txtScore = findViewById(R.id.txt_score)
        progressQuiz = findViewById(R.id.progress_quiz)
        layoutQuestion = findViewById(R.id.layout_question)
        txtNumeroQuestion = findViewById(R.id.txt_numero_question)
        imgQuestion = findViewById(R.id.img_question)
        txtQuestion = findViewById(R.id.txt_question)
        btnChoix1 = findViewById(R.id.btn_choix1)
        btnChoix2 = findViewById(R.id.btn_choix2)
        btnChoix3 = findViewById(R.id.btn_choix3)
        txtFeedback = findViewById(R.id.txt_feedback)
        btnSuivant = findViewById(R.id.btn_suivant)
        layoutResultat = findViewById(R.id.layout_resultat)
        txtResultatScore = findViewById(R.id.txt_resultat_score)
        btnRejouer = findViewById(R.id.btn_rejouer)

        boutonsChoix = listOf(btnChoix1, btnChoix2, btnChoix3)
    }

    private fun configurerClics() {
        btnChoix1.setOnClickListener { validerReponse(0) }
        btnChoix2.setOnClickListener { validerReponse(1) }
        btnChoix3.setOnClickListener { validerReponse(2) }

        btnSuivant.setOnClickListener {
            if (indexQuestionCourante < questions.size - 1) {
                indexQuestionCourante++
                afficherQuestionCourante()
            } else {
                afficherResultatFinal()
            }
        }

        btnRejouer.setOnClickListener {
            rejouer()
        }
    }

    private fun afficherQuestionCourante() {
        aDejaRepondu = false

        val question = questions[indexQuestionCourante]

        txtNumeroQuestion.text = getString(
            R.string.question_format, indexQuestionCourante + 1, questions.size
        )
        imgQuestion.setImageResource(question.image)
        imgQuestion.contentDescription = question.description
        txtQuestion.text = question.texte

        btnChoix1.text = question.choix[0]
        btnChoix2.text = question.choix[1]
        btnChoix3.text = question.choix[2]

        boutonsChoix.forEach { it.isEnabled = true }

        txtFeedback.visibility = android.view.View.INVISIBLE

        btnSuivant.isEnabled = false
        btnSuivant.text = if (indexQuestionCourante == questions.size - 1) {
            getString(R.string.bouton_voir_resultat)
        } else {
            getString(R.string.bouton_suivant)
        }

        actualiserBandeau()
    }

    private fun validerReponse(indexChoisi: Int) {

        if (aDejaRepondu) return
        aDejaRepondu = true

        val question = questions[indexQuestionCourante]
        val estCorrecte = indexChoisi == question.bonneReponse

        if (estCorrecte) {
            score++
            txtFeedback.text = getString(R.string.reponse_correcte)
            txtFeedback.setTextColor(
                ContextCompat.getColor(this, R.color.reponse_correcte_couleur)
            )
        } else {
            txtFeedback.text = getString(
                R.string.reponse_incorrecte_format, question.choix[question.bonneReponse]
            )
            txtFeedback.setTextColor(
                ContextCompat.getColor(this, R.color.reponse_incorrecte_couleur)
            )
        }

        txtFeedback.visibility = android.view.View.VISIBLE

        boutonsChoix.forEach { it.isEnabled = false }

        actualiserBandeau()

        btnSuivant.isEnabled = true
    }

    private fun actualiserBandeau() {
        txtScore.text = getString(R.string.score_format, score, questions.size)

        val nombreReponses = if (aDejaRepondu) {
            indexQuestionCourante + 1
        } else {
            indexQuestionCourante
        }
        progressQuiz.progress = nombreReponses
    }

    private fun afficherResultatFinal() {
        layoutQuestion.visibility = android.view.View.GONE
        layoutResultat.visibility = android.view.View.VISIBLE

        txtResultatScore.text = getString(
            R.string.resultat_score_format, score, questions.size
        )
        progressQuiz.progress = questions.size
    }

    private fun rejouer() {
        score = 0
        indexQuestionCourante = 0

        layoutResultat.visibility = android.view.View.GONE
        layoutQuestion.visibility = android.view.View.VISIBLE

        afficherQuestionCourante()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLE_INDEX, indexQuestionCourante)
        outState.putInt(CLE_SCORE, score)
    }

    companion object {
        private const val CLE_INDEX = "cle_index_question"
        private const val CLE_SCORE = "cle_score"
    }
}