package com.example.expensesmanager.model

import com.google.firebase.database.PropertyName

class UnData(
    @get:PropertyName("ourAmountInt") @set:PropertyName("ourAmountInt")
    var ourAmountInt: Int = 0,

    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("note") @set:PropertyName("note")
    var note: String = "",

    @get:PropertyName("to") @set:PropertyName("to")
    var to: String = "",

    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = ""
) {
    constructor() : this(0, "", "", "", "")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnData

        if (ourAmountInt != other.ourAmountInt) return false
        if (type != other.type) return false
        if (note != other.note) return false
        if (to != other.to) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ourAmountInt
        result = 31 * result + type.hashCode()
        result = 31 * result + note.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
