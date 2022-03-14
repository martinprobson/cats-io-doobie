package net.martinprobson.catsdoobie.example.repository

import net.martinprobson.catsdoobie.example.model.{Pet, PetWithId}
import net.martinprobson.catsdoobie.example.model.PetWithId.ID

import doobie.*
import doobie.implicits.*

import cats.effect.*
import cats.syntax.all.*

class DoobiePetRepository(xa: Transactor[IO]) extends PetRepository {

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

  def addPet(pet: Pet): IO[ID] = insert(xa, pet)

  //def addPets(pets: List[Pet]): IO[List[ID]] = pets.traverse(pet => addPet(pet))
  def addPets(pets: List[Pet]): IO[List[ID]] = pets.traverse(pet => insert(xa, pet))

  def getPet(id: ID): IO[Option[PetWithId]] = select(id).transact(xa)

  def getPetByName(name: String): IO[List[PetWithId]] = selectByName(name).transact(xa)

  def getPets: IO[Seq[PetWithId]] = selectAll.transact(xa)

  def countPets: IO[Long] = selectCount.transact(xa)

}

object DoobiePetRepository {
  def apply(xa: Transactor[IO]): IO[DoobiePetRepository] =
    IO(new DoobiePetRepository(xa))
}
