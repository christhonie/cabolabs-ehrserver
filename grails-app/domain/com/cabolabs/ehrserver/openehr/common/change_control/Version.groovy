/*
 * Copyright 2011-2017 CaboLabs Health Informatics
 *
 * The EHRServer was designed and developed by Pablo Pazos Gutierrez <pablo.pazos@cabolabs.com> at CaboLabs Health Informatics (www.cabolabs.com).
 *
 * You can't remove this notice from the source code, you can't remove the "Powered by CaboLabs" from the UI, you can't remove this notice from the window that appears then the "Powered by CaboLabs" link is clicked.
 *
 * Any modifications to the provided source code can be stated below this notice.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cabolabs.ehrserver.openehr.common.change_control

import com.cabolabs.ehrserver.openehr.common.generic.AuditDetails
import com.cabolabs.ehrserver.ehr.clinical_documents.CompositionIndex
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAccessType

// FIXME: this is the representation of a VERSION<COMPOSITION> not VERSION<T>
class Version {

   // Now assigned by the client:
   // https://github.com/ppazos/cabolabs-ehrserver/issues/50
   //
   // FIXME: https://github.com/ppazos/cabolabs-ehrserver/issues/52
   //
   // object_id, creating_system_id and version_tree_id.
   //
   // Emula ORIGINAL_VERSION.uid, ORIGINAL_VERSION hereda de VERSION
   // - object_id (id del VERSIONED_OBJECT),
   // - creating_system_id (identificador del sistema donde se creó la versión) and
   // - version_tree_id (es el número de version y branch que se crea cuando se piden datos para modificar
   // (no creo que sea necesario tener asignar números de branch cuando se piden datos solo para leer)).
   // Como no hay modificaciones (por ahora) el version_tree_id siempre va a ser 1
   // (debe ser asignado por el servidor cuando se hace commit de un documento).
   //
   // Ej. 591eb8e8-3a65-4630-a2e9-ffdeafc9bbba::10aec661-5458-4ff6-8e63-c2265537196d::1
   //
   // El id lo establece el EHR Server cuando recibe un commit.
   //
   String uid
   String precedingVersionUid

   // Emula ORIGINAL_VERSION.lifecycle_state.code_string
   String lifecycleState

   AuditDetails commitAudit

   // Datos commiteados (referencia a la composition)
   CompositionIndex data

   Contribution contribution

   String fileLocation

   /**
    * +1 on the uid.versionTreeId.trunkVersion, it is used to generate a new uid for the new version.
    * @return
    */
   def addTrunkVersion()
   {
      def newUid = this.objectId +"::"+
                   this.creatingSystemId +"::"+
                   (new Integer(this.versionTreeId) + 1).toString()

      this.uid = newUid
   }


   // These methods emulate version.uid.[objectId, creatingSystemId, treeVersionId]

   /**
    * id de la composition que contiene la version
    *
    * @return String UUID
    */
   def getObjectId()
   {
      return uid.split("::")[0]
   }

   /**
    * id del sistema que commitea los datos (donde fue creada la version)
    *
    * @return String
    */
   def getCreatingSystemId()
   {
      return uid.split("::")[1]
   }

   def getVersionTreeId()
   {
      return uid.split("::")[2]
   }

   static transients = ['objectId', 'creatingSystemId', 'versionTreeId']

   static belongsTo = [Contribution]

   static constraints = {
      contribution(nullable: false) // La version debe estar dentro de una contribution
      lifecycleState(inList: ['532', '553', '523']) // complete, incomplete, deleted
      precedingVersionUid(nullable: true)
      fileLocation(maxSize:1024)
   }

   static namedQueries = {
      byOrgInPeriod { uid, from, to ->
         contribution {
            eq('organizationUid', uid)
         }
         commitAudit {
            ge('timeCommitted', from) // dfrom <= timeCommitted < dto
            lt('timeCommitted', to)
         }
      }
   }
}
