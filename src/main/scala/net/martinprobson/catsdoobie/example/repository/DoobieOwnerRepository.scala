package net.martinprobson.catsdoobie.example.repository
import cats.effect.*
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import net.martinprobson.catsdoobie.example.model.Owner
import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID

class DoobieOwnerRepository(xa: Transactor[IO]) extends OwnerRepository {

  private def insert(owner: Owner): IO[OWNER_ID] = (for {
    _ <- sql"INSERT INTO owner (name) VALUES (${owner.name})".update.run
    id <- sql"SELECT last_insert_id()".query[Long].unique
  } yield id).transact(xa)

  private def select(id: OWNER_ID): ConnectionIO[Option[Owner]] =
    sql"SELECT id, name FROM owner WHERE id = $id".query[Owner].option

  private def selectCount: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM owner".query[Long].unique

  private def selectAll: ConnectionIO[List[Owner]] =
    sql"SELECT id, name FROM owner".query[Owner].stream.compile.toList

  private def selectByName(name: String): ConnectionIO[List[Owner]] =
    sql"SELECT id, name FROM owner WHERE name = $name"
      .query[Owner]
      .stream
      .compile
      .toList

  override def addOwner(owner: Owner): IO[OWNER_ID] = insert(owner)

  override def addOwners(owners: List[Owner]): IO[List[OWNER_ID]] = owners.traverse(addOwner)

  override def getOwner(id: OWNER_ID): IO[Option[Owner]] = select(id).transact(xa)

  override def getOwnerByName(name: String): IO[List[Owner]] = selectByName(name).transact(xa)

  override def getOwners: IO[List[Owner]] = selectAll.transact(xa)

  override def countOwners: IO[Long] = selectCount.transact(xa)
}

object DoobieOwnerRepository {
  def apply(xa: Transactor[IO]): IO[DoobieOwnerRepository] =
    IO(new DoobieOwnerRepository(xa))
}
