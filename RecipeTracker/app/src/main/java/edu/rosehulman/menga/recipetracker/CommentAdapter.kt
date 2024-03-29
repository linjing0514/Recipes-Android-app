package edu.rosehulman.menga.recipetracker

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class CommentAdapter(var context: Context?, val user: FirebaseUser, val recipeID:String?) : RecyclerView.Adapter<CommentViewHolder>() {
    val comments = ArrayList<Comment>()
    val commentRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.COMMENT)

    init {
        commentRef
            .orderBy(Comment.CREATION_KEY, Query.Direction.DESCENDING)
            .whereEqualTo("recipeId",recipeID)
            .addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
                if (exception != null) {
                    Log.e(Constants.TAG, "Listen error: $exception")
                    return@addSnapshotListener
                }
                for (documentChange in snapshot!!.documentChanges) {
                    if(documentChange.document["recipeId"] != recipeID) {
                        return@addSnapshotListener
                    }
                    val comment = Comment.fromSnapshot(documentChange.document)
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            comments.add(0, comment)
                            notifyItemInserted(0)
                        }
                        DocumentChange.Type.REMOVED -> {
                            val pos = comments.indexOfFirst { comment.id == it.id }
                            comments.removeAt(pos)
                            notifyItemRemoved(pos)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val pos = comments.indexOfFirst { comment.id == it.id }
                            comments[pos] = comment
                            notifyItemChanged(pos)
                        }
                    }
                }
            }
        update()
    }

    fun update() {
        commentRef
            .orderBy(Comment.CREATION_KEY, Query.Direction.DESCENDING)
            .whereEqualTo("recipeId", recipeID).get()
            .addOnSuccessListener {
                comments.clear()
                for(doc in it.documents) {
                    val comment = Comment.fromSnapshot(doc)
                    comments.add(comment)
                }
                notifyDataSetChanged()
            }
    }

    override fun getItemCount() = comments.size

    override fun onCreateViewHolder(parent: ViewGroup, index: Int): CommentViewHolder {
        Log.d(Constants.TAG,"Creating view holder for comment")
        val view = LayoutInflater.from(context).inflate(R.layout.row_comment, parent, false)
        return CommentViewHolder(view,this,context!!)
    }

    override fun onBindViewHolder(viewHolder: CommentViewHolder, index: Int) {
        viewHolder.bind(comments[index])
    }

    fun editCommentDialog(position: Int = -1){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_edit_title)

        val commentEditText = EditText(context)
        commentEditText.setText(comments[position].content)
        builder.setView(commentEditText)

        builder.setPositiveButton(android.R.string.ok){_, _ ->
            val content = commentEditText.text.toString()
            edit(position,content)
            notifyItemChanged(position)
        }
        builder.setNeutralButton(context?.getString(R.string.delete)){_,_ ->
            remove(position)
            notifyItemRemoved(position)
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.create().show()
    }
    private fun remove(position: Int){
        commentRef.document(comments[position].id).delete()
    }

    private  fun edit(position: Int,content:String){
        comments[position].content = content
        commentRef.document(comments[position].id).set(comments[position])
    }

    fun add(comment: Comment) {
        commentRef.add(comment)
    }

    fun authMessage() {
        Toast.makeText((context as MainActivity), context!!.resources.getString(R.string.comment_warning),Toast.LENGTH_SHORT).show()
    }
}