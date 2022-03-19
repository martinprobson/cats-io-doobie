package net.martinprobson.catsdoobie.example.repository

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import net.martinprobson.catsdoobie.example.model.Pet
import net.martinprobson.catsdoobie.example.model.Pet.PET_ID

class InMemoryPetRepository(db: Ref[IO, Map[PET_ID, Pet]], ownerRepository: IO[OwnerRepository])
    extends PetRepository {

  override def addPet(pet: Pet): IO[PET_ID] = for {
    id <- generateId
    owner <- ownerRepository.flatMap(or => or.getOrAdd(pet.owner))
    _ <- db.update(pets => pets.updated(key = id, value = pet.copy(owner = owner))).as(id)
  } yield id

  override def addPets(pets: List[Pet]): IO[List[PET_ID]] = pets.traverse(addPet)

  override def getPet(id: PET_ID): IO[Option[Pet]] = db.get.map { pets =>
    pets.get(key = id).map { pet => Pet(id, pet.name, pet.owner) }
  }

  override def getPetByName(name: String): IO[List[Pet]] = db.get.map { pets =>
    pets
      .filter { case (_, pet) => pet.name == name }
      .map { case (id, pet) => Pet(id, pet.name, pet.owner) }
      .toList
  }

  override def getPets: IO[List[Pet]] = db.get.map { pets =>
    pets.map { case (id, pet) => Pet(id, pet.name, pet.owner) }.toList
  }

  override def countPets: IO[Long] = db.get.flatMap { pets => IO(pets.size.toLong) }
}

object InMemoryPetRepository {
  def empty(ownerRepository: IO[OwnerRepository]): IO[PetRepository] =
    Ref[IO]
      .of(Map.empty[PET_ID, Pet])
      .map(ref => new InMemoryPetRepository(db = ref, ownerRepository))
}
