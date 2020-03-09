package com.ibashkimi.wheel.firestore

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun DocumentReference.asFlow(): Flow<DocumentSnapshot> =
    documentSnapshotFlow(this)

fun Query.asFlow(): Flow<QuerySnapshot> =
    querySnapshotFlow(this)

private fun documentSnapshotFlow(reference: DocumentReference): Flow<DocumentSnapshot> =
    callbackFlow {
        val eventListener = EventListener<DocumentSnapshot> { documentSnapshot, exception ->
            if (exception != null) {
                close(exception)
            } else {
                offer(documentSnapshot!!)
            }
        }
        val listener = reference.addSnapshotListener(eventListener)
        awaitClose { listener.remove() }
    }

private fun collectionSnapshotFlow(reference: DocumentReference): Flow<DocumentSnapshot> =
    callbackFlow {
        val eventListener = EventListener<DocumentSnapshot> { documentSnapshot, exception ->
            if (exception != null) {
                close(exception)
            } else {
                offer(documentSnapshot!!)
            }
        }
        val listener = reference.addSnapshotListener(eventListener)
        awaitClose { listener.remove() }
    }

private fun querySnapshotFlow(query: Query): Flow<QuerySnapshot> = callbackFlow {
    val eventListener = EventListener<QuerySnapshot> { querySnapshot, exception ->
        if (exception != null) {
            close(exception)
        } else {
            offer(querySnapshot!!)
        }
    }
    val listener = query.addSnapshotListener(eventListener)
    awaitClose { listener.remove() }
}

fun DocumentReference.writeFlow(data: Any): Flow<String> =
    writeNullFlow(this, { data })

fun DocumentReference.deleteAsFlow(): Flow<Unit> = callbackFlow {
    delete()
        .addOnCanceledListener { close() }
        .addOnFailureListener { close(it) }
        .addOnSuccessListener { if (!isClosedForSend) offer(Unit) }
}

fun Task<Void>.asFlow(): Flow<Unit> = callbackFlow {
    this@asFlow
        .addOnCanceledListener { close() }
        .addOnFailureListener { close(it) }
        .addOnSuccessListener { if (!isClosedForSend) offer(Unit) }
}

fun writeNullFlow(
    documentReference: DocumentReference,
    toMap: () -> Any
): Flow<String> = callbackFlow {
    documentReference.set(toMap())
        .addOnSuccessListener { offer(documentReference.id) }
        .addOnFailureListener { close(it) }
        .addOnCanceledListener { close() }
    awaitClose { }
}

// todo limit this "query" to 1 item. to many reads
fun Query.changes(): Flow<Unit> = callbackFlow {
    val eventListener = EventListener<QuerySnapshot> { _, exception ->
        if (exception != null) {
            close(exception)
        } else {
            offer(Unit)
        }
    }
    val listener = this@changes.addSnapshotListener(eventListener)
    awaitClose { listener.remove() }
}

/** Delete a collection in batches to avoid out-of-memory errors.
 * Batch size may be tuned based on document size (atmost 1MB) and application requirements.
 */
tailrec suspend fun CollectionReference.deleteCollection(batchSize: Long) {
    val documents = this.getDocuments(batchSize)
    var deleted = 0

    documents.forEach {
        it.reference.delete()
        ++deleted
    }

    if (deleted >= batchSize) { // retrieve and delete another batch
        deleteCollection(batchSize)
    }
}

suspend fun CollectionReference.getDocuments(batchSize: Long): List<DocumentSnapshot> {
    val documents = CompletableDeferred<List<DocumentSnapshot>>()
    limit(batchSize).get()
        .addOnSuccessListener { documents.complete(it.documents) }
        .addOnFailureListener { documents.completeExceptionally(it) }
    return documents.await()
}

fun Query.withContinuation(continuation: Any?): Query {
    return if (continuation != null && continuation is QueryDocumentSnapshot) {
        startAfter(continuation)
    } else {
        this
    }
}