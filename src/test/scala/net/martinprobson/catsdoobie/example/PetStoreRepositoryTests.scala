package net.martinprobson.catsdoobie.example

import cats.effect.testing.scalatest.AsyncIOSpec
import net.martinprobson.catsdoobie.example.model.{Owner, Pet}
import net.martinprobson.catsdoobie.example.repository.PetStoreRepository
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class PetStoreRepositoryTests extends AsyncFunSuite with AsyncIOSpec {

  test("countPets <> 0") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      _ <- petStoreRepository.addPet(Pet("Pet1", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet2", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet3", Owner("Owner 1")))
      count <- petStoreRepository.countPets
    } yield count).asserting(_ shouldBe 3)
  }

  test("countPets == 0") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      count <- petStoreRepository.countPets
    } yield count).asserting(_ shouldBe 0)
  }

  test("addPet/getPet") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      id <- petStoreRepository.addPet(Pet("Pet1", Owner("Owner 1")))
      pet <- petStoreRepository.getPet(id)
      owner <- petStoreRepository.getOwner(1)
    } yield (pet, owner)).asserting {
      case (Some(Pet(_, "Pet1", Owner(1, "Owner 1"))), Some(Owner(1, "Owner 1"))) => assert(true)
      case _                                                                      => fail("Fail")
    }
  }

  test("addPets") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      _ <- petStoreRepository.addPet(Pet("Pet1", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet2", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet3", Owner("Owner 1")))
      petCount <- petStoreRepository.countPets
      ownerCount <- petStoreRepository.countOwners
    } yield (petCount, ownerCount)).asserting(_ shouldBe (3, 1))
  }

  test("addPet/getPetByName/getOwnerByName") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      _ <- petStoreRepository.addPet(Pet("Pet1", Owner("Owner 1")))
      pet <- petStoreRepository.getPetByName("Pet1")
      owner <- petStoreRepository.getOwnerByName("Owner 1")
    } yield (pet, owner)).asserting {
      case (List(Pet(_, "Pet1", Owner(1, "Owner 1"))), List(Owner(1, "Owner 1"))) => assert(true)
      case _                                                                      => fail("Fail")
    }
  }

  test("getPets") {
    (for {
      petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
      _ <- petStoreRepository.addPet(Pet("Pet1", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet2", Owner("Owner 1")))
      _ <- petStoreRepository.addPet(Pet("Pet3", Owner("Owner 1")))
      pets <- petStoreRepository.getPets
    } yield pets).asserting {
      case List(
            Pet(_, "Pet1", Owner(1, "Owner 1")),
            Pet(_, "Pet2", Owner(1, "Owner 1")),
            Pet(_, "Pet3", Owner(1, "Owner 1"))
          ) =>
        assert(true)
      case _ => fail("Fail")
    }
  }
}
