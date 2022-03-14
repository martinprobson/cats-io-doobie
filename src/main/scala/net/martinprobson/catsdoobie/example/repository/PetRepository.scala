package net.martinprobson.catsdoobie.example.repository

import cats.effect.IO
import net.martinprobson.catsdoobie.example.model.{Pet, PetWithId}
import net.martinprobson.catsdoobie.example.model.PetWithId.ID
import java.util.UUID

trait PetRepository {

  def generateId: IO[ID] = IO(UUID.randomUUID().toString)
  def addPet(pet: Pet): IO[ID]
  def addPets(pets: List[Pet]): IO[List[ID]]
  def getPet(id: ID): IO[Option[PetWithId]]
  def getPetByName(name: String): IO[List[PetWithId]]
  def getPets: IO[Seq[PetWithId]]
  def countPets: IO[Long]

}
