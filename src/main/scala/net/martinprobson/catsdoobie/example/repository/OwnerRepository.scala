package net.martinprobson.catsdoobie.example.repository

import cats.effect.IO
import net.martinprobson.catsdoobie.example.model.Owner.OWNER_ID
import net.martinprobson.catsdoobie.example.model.Owner

trait OwnerRepository {

  def addOwner(owner: Owner): IO[OWNER_ID]
  def addOwners(owners: List[Owner]): IO[List[OWNER_ID]]
  def getOwner(id: OWNER_ID): IO[Option[Owner]]
  def getOwnerByName(name: String): IO[List[Owner]]
  def getOwners: IO[List[Owner]]
  def countOwners: IO[Long]
  def getOrAdd(owner: Owner): IO[Owner] = for {
    ownerList <- getOwnerByName(owner.name)
    newowner <-
      ownerList.headOption match {
        case Some(o) => IO(o)
        case None    => addOwner(owner).map(i => Owner(i, owner.name))
      }
  } yield newowner
}
