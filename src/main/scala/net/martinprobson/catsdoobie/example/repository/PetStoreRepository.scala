package net.martinprobson.catsdoobie.example.repository
import cats.effect.IO
import doobie.Transactor
import doobie.implicits.toSqlInterpolator
import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID
import net.martinprobson.catsdoobie.example.model.Pet.PET_ID
import net.martinprobson.catsdoobie.example.model.{Owner, Pet}

class PetStoreRepository(petRepository: IO[PetRepository], ownerRepository: IO[OwnerRepository])
    extends PetRepository
    with OwnerRepository {

  override def addPet(pet: Pet): IO[PET_ID] = petRepository.flatMap(_.addPet(pet))

  override def addPets(pets: List[Pet]): IO[List[PET_ID]] =
    petRepository.flatMap(_.addPets(pets))

  override def getPet(id: PET_ID): IO[Option[Pet]] = petRepository.flatMap(_.getPet(id))

  override def getPetByName(name: String): IO[List[Pet]] =
    petRepository.flatMap(_.getPetByName(name))

  override def getPets: IO[List[Pet]] = petRepository.flatMap(_.getPets)

  override def countPets: IO[Long] = petRepository.flatMap(_.countPets)

  override def addOwner(owner: Owner): IO[OWNER_ID] =
    ownerRepository.flatMap(_.addOwner(owner))

  override def addOwners(owners: List[Owner]): IO[List[OWNER_ID]] =
    ownerRepository.flatMap(_.addOwners(owners))

  override def getOwner(id: OWNER_ID): IO[Option[Owner]] =
    ownerRepository.flatMap(_.getOwner(id))

  override def getOwnerByName(name: String): IO[List[Owner]] =
    ownerRepository.flatMap(_.getOwnerByName(name))

  override def getOwners: IO[List[Owner]] = ownerRepository.flatMap(_.getOwners)

  override def countOwners: IO[Long] = ownerRepository.flatMap(_.countOwners)

}

object PetStoreRepository {
  def inMemoryPetStoreReposiory: IO[PetStoreRepository] = for {
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
