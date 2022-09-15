package net.martinprobson.catsdoobie.example.repository

import cats.effect.IO
import net.martinprobson.catsdoobie.example.model.Pet
import net.martinprobson.catsdoobie.example.model.Pet.PET_ID
import java.util.UUID

trait PetRepository {

  def generateId: IO[PET_ID] = IO(UUID.randomUUID().toString)
  def addPet(pet: Pet): IO[PET_ID]
  def addPets(pets: List[Pet]): IO[List[PET_ID]]
  def getPet(id: PET_ID): IO[Option[Pet]]
  def getPetByName(name: String): IO[List[Pet]]
  def getPets: IO[List[Pet]]

  def getPets(limit: Int): IO[List[Pet]]

  def countPets: IO[Long]
}
