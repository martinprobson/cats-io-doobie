package net.martinprobson.catsdoobie.example.repository

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import net.martinprobson.catsdoobie.example.model.{Pet, PetWithId}
import net.martinprobson.catsdoobie.example.model.PetWithId.ID

class InMemoryPetRepository(db: Ref[IO, Map[ID, Pet]]) extends PetRepository {

  override def addPet(pet: Pet): IO[ID] =
    generateId.flatMap { id =>
      db.update(pets => pets.updated(key = id, value = pet)).as(id)
    }

  override def addPets(pets: List[Pet]): IO[List[ID]] = pets.traverse(pet => addPet(pet))

  override def getPet(id: ID): IO[Option[PetWithId]] = db.get.map { pets =>
    pets.get(key = id).map { pet => PetWithId(id, pet.name) }
  }

  override def getPetByName(name: String): IO[List[PetWithId]] = db.get.map { pets =>
    pets
      .filter { case (_, pet) => pet.name == name }
      .map { case (id, pet) => PetWithId(id, pet.name) }
      .toList
  }

  override def getPets: IO[Seq[PetWithId]] = db.get.map { pets =>
    pets.map { case (id, pet) => PetWithId(id, pet.name) }.toList
  }

  override def countPets: IO[Long] = db.get.flatMap { pets => IO(pets.size.toLong) }
}

object InMemoryPetRepository {
  def empty: IO[PetRepository] =
    Ref[IO]
      .of(Map.empty[ID, Pet])
      .map(ref => new InMemoryPetRepository(db = ref))
}
