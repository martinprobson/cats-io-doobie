import cats.effect.{IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import net.martinprobson.catsdoobie.example.config.Logging
import net.martinprobson.catsdoobie.example.model.{Owner, Pet}
import net.martinprobson.catsdoobie.example.repository.PetStoreRepository

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
//        ce
//      )
    } yield xa

  def run: IO[Unit] = transactor.use { xa => program(xa) }

  def program(xa: Transactor[IO]): IO[Unit] = for {
    petStoreRepository <- PetStoreRepository.doobiePetStoreRepository(xa)
    _ <- petStoreRepository.addPet(Pet("Hammy1", Owner("Hammy's owner 2")))
    _ <- petStoreRepository.addPet(Pet("Hammy2", Owner("Hammy's owner 2")))
    _ <- petStoreRepository.addPet(Pet("Hammy3", Owner("Hammy's owner 30")))
    pets <- petStoreRepository.getPets
    _ <- IO(pets.foreach(println))
    owners <- petStoreRepository.getOwners
    _ <- IO(owners.foreach(println))
  } yield ()
}
