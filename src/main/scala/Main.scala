import cats.effect.{IO, IOApp, Resource}
import cats.implicits.*
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import net.martinprobson.catsdoobie.example.config.Logging
import net.martinprobson.catsdoobie.example.model.Pet
import net.martinprobson.catsdoobie.example.repository.{DoobiePetRepository, InMemoryPetRepository}

import java.util.UUID

object Main extends IOApp.Simple with Logging {

  private val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "com.mysql.cj.jdbc.Driver",
        "jdbc:mysql://localhost:3306/test2",
        "root",
        "root",
        ce
      )
//      xa <- HikariTransactor.newHikariTransactor[IO](
//        "org.h2.Driver",
//        "jdbc:h2:mem:db;DB_CLOSE_DELAY=-1",
//        "sa",
//        "sa",
//        ce)
    } yield xa

  def run: IO[Unit] = for {
    petRepository <- DoobiePetRepository(transactor)
    //petRepository <- InMemoryPetRepository.empty
    //id <- petRepository.addPet(Pet("Fred")) // pool-1
    //_ <- IO.println(s"id = $id")
    //pet <- petRepository.getPet(id) // pool-2
    //_ <- IO.println(pet)
    //pet3 <- petRepository.getPet(UUID.randomUUID.toString) // pool-3
    //_ <- IO.println(pet3)
    //ids <- petRepository.addPets(
    //  Range(1, 10).map(_.toString).toList.map(name => Pet(name))
    //) // pool-4
    //_ <- IO.println(s"ids = $ids")
    _ <- {
      val a = List(
        petRepository.addPets(
          Range(1, 2000).map(_.toString).toList.map(name => Pet(name))
        ), // pool-5
        petRepository.addPets(
          Range(1, 2000).map(_.toString).toList.map(name => Pet(name))
        ), // pool-6
        petRepository.addPets(
          Range(1, 2000).map(_.toString).toList.map(name => Pet(name))
        ), // pool-7
        petRepository.addPets(
          Range(1, 2000).map(_.toString).toList.map(name => Pet(name))
        ), // pool-8
        petRepository.addPets(
          Range(1, 2000).map(_.toString).toList.map(name => Pet(name))
        ) // pool-9
      )
      a.parSequence_
    }
    //count <- petRepository.countPets // pool-10
    //_ <- IO.println(s"Total number: $count")
  } yield ()
}
