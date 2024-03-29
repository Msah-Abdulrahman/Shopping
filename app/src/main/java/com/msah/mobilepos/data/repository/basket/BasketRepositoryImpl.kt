package com.msah.mobilepos.data.repository.basket

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msah.mobilepos.data.model.Order
import com.msah.mobilepos.data.model.ProductBasket
import com.msah.mobilepos.utils.Constants
import com.msah.mobilepos.utils.Constants.DATABASE_BASKET_STATUS_FIELD
import com.msah.mobilepos.utils.Constants.DATABASE_PRODUCTS_TABLE_PIECE_FIELD

class BasketRepositoryImpl : BasketRepository {

    override fun getAllProductsBasket(): CollectionReference {

        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)

    }

    override fun getTargetProductsBasket(productBasket: ProductBasket): DocumentReference {

        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)
            .document(productBasket.id.toString())
    }

    override fun addProductsToBasket(productBasket: ProductBasket): Task<Void> {

        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)
            .document(productBasket.id.toString())
            .set(productBasket)

    }



    fun updateCartStatus(productId: String, newStatus: String): Task<Void> {
        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)
            .document(productId)
            .update("cartStatus", newStatus)
    }

    override fun addProductToOrder(order: Order, orderId: String): Task<DocumentReference> {
        return Firebase.firestore.collection("orders").add(order)

    }


    override fun deleteProducts(productBasket: ProductBasket): Task<Void> {

        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)
            .document(productBasket.id.toString())
            .delete()

    }



    override fun updateProductsPiece(productBasket: ProductBasket): Task<Void> {

        return Firebase.firestore.collection(Constants.DATABASE_BASKET_TABLE)
            .document(FirebaseAuth.getInstance().uid!!)
            .collection(Constants.DATABASE_PRODUCTS_TABLE)
            .document(productBasket.id.toString())
            .update(DATABASE_PRODUCTS_TABLE_PIECE_FIELD, productBasket.piece)

    }


}