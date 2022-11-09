package net.martinprobson.catsdoobie.example.repository

import net.martinprobson.catsdoobie.example.model.Pet
import net.martinprobson.catsdoobie.example.model.Pet.PET_ID
import doobie._
import doobie.implicits._
import cats.effect._
import cats.syntax.all._

class DoobiePetRepository(xa: Transactor[IO], ownerRepository: IO[OwnerRepository])
    extends PetRepository {

  private def insert(pet: Pet): IO[PET_ID] = for {
    id <- generateId
    owner <- ownerRepository.flatMap(or => or.getOrAdd(pet.owner))
    _ <- sql"INSERT INTO pet (id, name, owner_id) VALUES ($id, ${pet.name}, ${owner.id})".update.run
      .transact(xa)
  } yield id

  private def select(id: PET_ID): ConnectionIO[Option[Pet]] =
    sql"""SELECT p.id,
                 p.name,
                 o.id,
                 o.name
          FROM   pet p
          JOIN   owner o ON p.owner_id = o.id
          WHERE  p.id = $id""".query[Pet].option

  private def selectCount: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM pet".query[Long].unique

  private def selectByName(name: String): ConnectionIO[List[Pet]] =
    sql"""SELECT p.id,
                 p.name,
                 o.id,
                 o.name
          FROM   pet p
          JOIN   owner o ON p.owner_id = o.id
          WHERE  p.name = $name""".query[Pet].stream.compile.toList

  private def selectAll: ConnectionIO[List[Pet]] =
    sql"""SELECT p.id,
                 p.name,
                 o.id,
                 o.name
          FROM   pet p
          JOIN   owner o ON p.owner_id = o.id""".query[Pet].stream.compile.toList

  def selectLimit(limit: Int): ConnectionIO[List[Pet]] =
    sql"""SELECT p.id,
                 p.name,
                 o.id,
                 o.name
          FROM   pet p
          JOIN   owner o ON p.owner_id = o.id""".query[Pet].stream.take(limit.toLong).compile.toList

  def addPet(pet: Pet): IO[PET_ID] = insert(pet)

  def addPets(pets: List[Pet]): IO[List[PET_ID]] = pets.traverse(insert)

  def getPet(id: PET_ID): IO[Option[Pet]] = select(id).transact(xa)

  def getPetByName(name: String): IO[List[Pet]] = selectByName(name).transact(xa)

  def getPets: IO[List[Pet]] = selectAll.transact(xa)

  def getPets(limit: Int): IO[List[Pet]] = selectLimit(limit).transact(xa)

  def countPets: IO[Long] = selectCount.transact(xa)

  def createTable: IO[Int] =
    sql"""
         |create table if not exists pet
         |(
         |    id       varchar(200) not null
         |        primary key,
         |    name     varchar(200) null,
         |    owner_id int          not null
         |);
         |""".stripMargin.update.run.transact(xa)
}

object DoobiePetRepository {
  def apply(xa: Transactor[IO]): IO[DoobiePetRepository] =
    IO(new DoobiePetRepository(xa, DoobieOwnerRepository(xa)))
}
