package fr.picotin.liquibase.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.TreeSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.StringUtils;

import com.google.common.collect.Sets;

import fr.picotin.liquibase.comparator.LiquibaseComparator;
import fr.picotin.liquibase.constants.LiquibaseConstants;
import fr.picotin.liquibase.dto.PluginOptionsDTO;

public class LiquibaseUtils {

	/**
	 * Create liquibase master changelog.
	 *
	 * @param pluginOptionsDTO
	 *            The DTO with Maven plugin Options
	 * @param changelogFiles
	 *            The list of changelog files
	 * @throws MojoExecutionException
	 *             An Exception
	 */
	public static void createLiquibaseMasterChangelog(final PluginOptionsDTO pluginOptionsDTO,
			final TreeSet<File> changelogFiles) throws MojoExecutionException {
		final String changelogMaster = "db.changelog-master-" + pluginOptionsDTO.sqlChangelogFormat + ".xml";
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(pluginOptionsDTO.filesLocation + File.separator + changelogMaster, "UTF-8");
			writer.println(LiquibaseConstants.XML_START.replace("${sqlType}", pluginOptionsDTO.sqlChangelogFormat)
					.replace("${liquibaseVersion}", pluginOptionsDTO.liquibaseVersion));

			for (final File file : changelogFiles) {
				writer.println("  <include file=\"" + file.getName() + "\" relativeToChangelogFile=\"true\"/>");
			}

			writer.println(LiquibaseConstants.XML_END);
		} catch (final FileNotFoundException e) {
			throw new MojoExecutionException(e.getMessage());
		} catch (final UnsupportedEncodingException e) {
			throw new MojoExecutionException(e.getMessage());
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Get liquibase changelog files.
	 *
	 * @param pluginOptionsDTO
	 *            The DTO with Maven plugin Options
	 * @return The list of files
	 * @throws MojoExecutionException
	 *             An exception
	 */
	public static TreeSet<File> getLiquibaseFiles(final PluginOptionsDTO pluginOptionsDTO)
			throws MojoExecutionException {

		final File dir = new File(pluginOptionsDTO.filesLocation);

		if (dir == null || !dir.isDirectory()) {
			throw new MojoExecutionException("Directory " + pluginOptionsDTO.filesLocation + " doesn't exists");
		}

		final FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (StringUtils.isNotEmpty(pluginOptionsDTO.filePattern)) {
					return name.matches(pluginOptionsDTO.filePattern);
				}
				return true;
			}
		};

		final TreeSet<File> files = Sets.newTreeSet(new LiquibaseComparator(pluginOptionsDTO.filePatternCustomSort));
		files.addAll(Arrays.asList(dir.listFiles(filter)));

		LiquibaseUtils.processCustomFilesToIgnore(pluginOptionsDTO, files);

		LiquibaseUtils.processCustomFilesToInsert(pluginOptionsDTO, files);

		return files;
	}

	/**
	 * Process custom files to ignore in master changelog.
	 *
	 * @param pluginOptionsDTO
	 *            The DTO with Maven plugin Options
	 * @param files
	 *            The files to add in master changelog
	 */
	private static void processCustomFilesToIgnore(final PluginOptionsDTO pluginOptionsDTO, final TreeSet<File> files) {

		if (StringUtils.isNotEmpty(pluginOptionsDTO.customFilesToIgnore)) {
			final String[] customFiles = pluginOptionsDTO.customFilesToIgnore.split(";");
			for (final String customFile : customFiles) {

				final File file = new File(pluginOptionsDTO.filesLocation + File.separator + customFile);
				if (file.exists()) {
					files.remove(file);
				}
			}
		}
	}

	/**
	 * Process custom files to insert in master changelog.
	 *
	 * @param pluginOptionsDTO
	 *            The DTO with Maven plugin Options
	 * @param files
	 *            The files to add in master changelog
	 */
	private static void processCustomFilesToInsert(final PluginOptionsDTO pluginOptionsDTO, final TreeSet<File> files) {

		if (StringUtils.isNotEmpty(pluginOptionsDTO.customFilesToInsert)) {
			final String[] customFiles = pluginOptionsDTO.customFilesToInsert.split(";");
			for (final String customFile : customFiles) {

				final File file = new File(pluginOptionsDTO.filesLocation + File.separator + customFile);
				if (file.exists()) {
					files.add(file);
				}
			}
		}
	}
}
