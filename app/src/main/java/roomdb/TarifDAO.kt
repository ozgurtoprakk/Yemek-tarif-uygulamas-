package roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import model.Tarif

@Dao
interface TarifDAO {
    @Query("SELECT * FROM Tarif")
    fun getAll():Flowable<List<Tarif>>

    @Query("SELECT * FROM Tarif WHERE id=:id")
    fun findById(id:Int):Flowable<Tarif>

    @Insert
    fun insert(tarif: Tarif):Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(tarif:Tarif):Completable

    @Delete
    fun delete(tarif: Tarif):Completable
}