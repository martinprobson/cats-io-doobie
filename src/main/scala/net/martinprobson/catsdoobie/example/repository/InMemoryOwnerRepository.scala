package net.martinprobson.catsdoobie.example.repository

import cats.effect.{IO, Ref}
import cats.implicits.toTraverseOps
import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID
import net.martinprobson.catsdoobie.example.model.Owner

class InMemoryOwnerRepository(db: Ref[IO, Map[OWNER_ID, Owner]], counter: Ref[IO, Long])
    extends OwnerRepository {

  override def addOwner(owner: Owner): IO[OWNER_ID] = for {
    id <- counter.modify(x => (x + 1, x + 1))
    _ <- db.update(owners => owners.updated(key = id, value = owner))
  } yield id

  override def addOwners(owners: List[Owner]): IO[List[OWNER_ID]] = owners.traverse(addOwner)

  override def getOwner(id: OWNER_ID): IO[Option[Owner]] =
    db.get.map { owners => owners.get(key = id).map { owner => Owner(id, owner.name) } }

  override def getOwnerByName(name: String): IO[List[Owner]] = db.get.map { owners =>
    owners
      .filter { case (_, owner) => owner.name == name }
      .map { case (id, owner) => Owner(id, owner.name) }
      .toList
  }

  override def getOwners: IO[List[Owner]] = db.get.map { owners =>
    owners.map { case (id, owner) => Owner(id, owner.name) }.toList
  }

  override def countOwners: IO[Long] = db.get.flatMap { owners => IO(owners.size.toLong) }

}

object InMemoryOwnerRepository {

  def empty: IO[OwnerRepository] = for {
    db <- Ref[IO].of(Map.empty[OWNER_ID, Owner])
    counter <- Ref[IO].of(0L)
  } yield new InMemoryOwnerRepository(db, counter)
}
