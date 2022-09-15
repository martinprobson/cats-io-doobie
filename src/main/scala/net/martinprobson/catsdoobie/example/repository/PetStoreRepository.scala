package net.martinprobson.catsdoobie.example.repository
import cats.effect.IO
import doobie.Transactor
import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID
import net.martinprobson.catsdoobie.example.model.Pet.PET_ID
import net.martinprobson.catsdoobie.example.model.{Owner, Pet}
import org.typelevel.log4cats.slf4j.Slf4jLogger

class PetStoreRepository(petRepository: IO[PetRepository], ownerRepository: IO[OwnerRepository]) {

  //def addPet(pet: Pet): IO[PET_ID] = petRepository.flatMap(_.addPet(pet))

  def addPet(pet: Pet): IO[PET_ID] = for {
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info(s"In addPet")
    p <- petRepository
    id <- p.addPet(pet)
  } yield id

  def addPets(pets: List[Pet]): IO[List[PET_ID]] =
    petRepository.flatMap(_.addPets(pets))

  def getPet(id: PET_ID): IO[Option[Pet]] = petRepository.flatMap(_.getPet(id))

  def getPetByName(name: String): IO[List[Pet]] =
    petRepository.flatMap(_.getPetByName(name))

  def getPets: IO[List[Pet]] = petRepository.flatMap(_.getPets)

  def getPets(limit: Int): IO[List[Pet]] = petRepository.flatMap(_.getPets(limit))

  def countPets: IO[Long] = petRepository.flatMap(_.countPets)

  def addOwner(owner: Owner): IO[OWNER_ID] =
    ownerRepository.flatMap(_.addOwner(owner))

  def addOwners(owners: List[Owner]): IO[List[OWNER_ID]] =
    ownerRepository.flatMap(_.addOwners(owners))

  def getOwner(id: OWNER_ID): IO[Option[Owner]] =
    ownerRepository.flatMap(_.getOwner(id))

  def getOwnerByName(name: String): IO[List[Owner]] =
    ownerRepository.flatMap(_.getOwnerByName(name))

  def getOwners: IO[List[Owner]] = ownerRepository.flatMap(_.getOwners)

  def countOwners: IO[Long] = ownerRepository.flatMap(_.countOwners)

}

object PetStoreRepository {
  def inMemoryPetStoreRepository: IO[PetStoreRepository] = for {
    ownerRepository <- InMemoryOwnerRepository.empty
    petRepository <- InMemoryPetRepository.empty(IO(ownerRepository))
  } yield new PetStoreRepository(IO(petRepository), IO(ownerRepository))

  def doobiePetStoreRepository(xa: Transactor[IO]): IO[PetStoreRepository] = for {
    ownerRepository <- DoobieOwnerRepository(xa)
    _ <- ownerRepository.createTable
    petRepository <- DoobiePetRepository(xa)
    _ <- petRepository.createTable
  } yield new PetStoreRepository(IO(petRepository), IO(ownerRepository))
}
