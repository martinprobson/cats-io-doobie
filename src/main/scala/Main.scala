import cats.effect.{IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.{ExecutionContexts, Transactor}
import net.martinprobson.catsdoobie.example.config.{Config, Logging}
import net.martinprobson.catsdoobie.example.model.{Owner, Pet}
import net.martinprobson.catsdoobie.example.repository.PetStoreRepository
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.instances.list.*
import cats.syntax.parallel.*

object Main extends IOApp.Simple with Logging {

  /** This is our main entry point where the code will actually get executed.
    *
    * We provide a transactor which will be used by Doobie to execute the SQL statements. Config is
    * lifted into a Resource so that it can be used to setup the connection pool.
    * @return
    */
  override def run: IO[Unit] = transactor(Resource.eval[IO, Config](Config.loadConfig))
    .use { xa => program(xa) }

  /** Setup a HikariTransactor connection pool.
    * @param config
    *   Config containing db connection parameters.
    * @return
    *   A Resource containing a HikariTransactor.
    */
  private def transactor(config: Resource[IO, Config]): Resource[IO, HikariTransactor[IO]] =
    for {
      cfg <- config
      ce <- ExecutionContexts.fixedThreadPool[IO](cfg.threads)
      xa <- HikariTransactor
        .newHikariTransactor[IO](
          cfg.jdbc.driverClassName,
          cfg.jdbc.url,
          cfg.jdbc.user,
          cfg.jdbc.password,
          ce
        )
    } yield xa

  //** Exercise our code by added some Pets/Owners in parallel.
  private def program(xa: Transactor[IO]): IO[Unit] = for {
    logger <- Slf4jLogger.create[IO]
    _ <- logger.info("Starting")
    petStoreRepository <- PetStoreRepository.doobiePetStoreRepository(xa)
    //petStoreRepository <- PetStoreRepository.inMemoryPetStoreRepository
    _ <- Range(1, 300).toList
      .parTraverse { i =>
        petStoreRepository.addPet(Pet(s"Name $i", Owner(s"Pet $i's owner")))
      }
    pets <- petStoreRepository.getPets
    _ <- IO(pets.foreach(println))
    ownerCount <- petStoreRepository.countOwners
    petCount <- petStoreRepository.countPets
    _ <- IO.println(s"Total Pets = $petCount, total Owners = $ownerCount")
  } yield ()
}
