package net.martinprobson.catsdoobie.example.repository

import net.martinprobson.catsdoobie.example.model.{Pet, PetWithId}
import net.martinprobson.catsdoobie.example.model.PetWithId.ID

import doobie.*
import doobie.implicits.*
import doobie.hikari.*

import cats.effect.*
import cats.syntax.all.*

class DoobiePetRepository(transactor: Resource[IO, HikariTransactor[IO]]) extends PetRepository {

  private def insert(xa: Transactor[IO], pet: Pet): IO[ID] = for {
    //_ <- IO.println(s"insert: ${Thread.currentThread.getName}")
    id <- generateId
    _ <- sql"INSERT INTO pet (id, name) VALUES ($id, ${pet.name})".update.run.transact(xa)
  } yield id

  private def select(id: ID): ConnectionIO[Option[PetWithId]] =
    sql"SELECT id, name FROM pet WHERE id = $id".query[PetWithId].option

  private def selectCount: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM pet".query[Long].unique

  private def selectByName(name: String): ConnectionIO[List[PetWithId]] =
    sql"SELECT id, name FROM pet WHERE name = $name"
      .query[PetWithId]
      .stream
      .compile
      .toList

  private def selectAll: ConnectionIO[List[PetWithId]] =
    sql"SELECT id, name FROM pet".query[PetWithId].stream.compile.toList

  def addPet(pet: Pet): IO[ID] = transactor.use { xa => insert(xa, pet) }

  //def addPets(pets: List[Pet]): IO[List[ID]] = pets.traverse(pet => addPet(pet))
  def addPets(pets: List[Pet]): IO[List[ID]] = transactor.use { xa =>
    pets.traverse(pet => insert(xa, pet))
  }

  def getPet(id: ID): IO[Option[PetWithId]] = transactor.use { xa => select(id).transact(xa) }

  def getPetByName(name: String): IO[List[PetWithId]] = transactor.use { xa =>
    selectByName(name).transact(xa)
  }

  def getPets: IO[Seq[PetWithId]] = transactor.use { xa => selectAll.transact(xa) }

  def countPets: IO[Long] = transactor.use { xa => selectCount.transact(xa) }

}

object DoobiePetRepository {
  def apply(transactor: Resource[IO, HikariTransactor[IO]]): IO[DoobiePetRepository] =
    IO(new DoobiePetRepository(transactor))
}
